<#import "field.ftl" as field>
<#import "footer.ftl" as loginFooter>

<#macro username>
    <#assign label>
        <#if !realm.loginWithEmailAllowed>
            ${msg("username")}
        <#elseif !realm.registrationEmailAsUsername>
            ${msg("usernameOrEmail")}
        <#else>
            ${msg("email")}
        </#if>
    </#assign>

    <@field.group name="username" label=label>
        <div class="${properties.kcInputGroup}">
            <div class="${properties.kcInputGroupItemClass} ${properties.kcFill}">
                <span class="${properties.kcInputClass} ${properties.kcFormReadOnlyClass}">
                    <input
                        id="kc-attempted-username"
                        value="${auth.attemptedUsername}"
                        readonly
                    >
                </span>
            </div>

            <div class="${properties.kcInputGroupItemClass}">
                <button
                    id="reset-login"
                    class="${properties.kcFormPasswordVisibilityButtonClass} kc-login-tooltip"
                    type="button"
                    aria-label="${msg('restartLoginTooltip')}"
                    onclick="location.href='${url.loginRestartFlowUrl}'"
                >
                    <i class="fa-sync-alt fas" aria-hidden="true"></i>
                    <span class="kc-tooltip-text">
                        ${msg("restartLoginTooltip")}
                    </span>
                </button>
            </div>
        </div>
    </@field.group>
</#macro>


<#macro registrationLayout
    bodyClass=""
    displayInfo=false
    displayMessage=true
    displayRequiredFields=false
>

<!DOCTYPE html>

<html
    class="${properties.kcHtmlClass!}"
    lang="${lang}"
    <#if realm.internationalizationEnabled>
        dir="${(locale.rtl)?then('rtl','ltr')}"
    </#if>
>

<head>

    <meta charset="utf-8">

    <meta
        http-equiv="Content-Type"
        content="text/html; charset=UTF-8"
    />

    <meta
        name="robots"
        content="noindex, nofollow"
    >

    <meta
        name="color-scheme"
        content="light"
    >

    <meta
        name="viewport"
        content="width=device-width, initial-scale=1"
    >

    <#if properties.meta?has_content>

        <#list properties.meta?split(' ') as meta>

            <meta
                name="${meta?split('==')[0]}"
                content="${meta?split('==')[1]}"
            />

        </#list>

    </#if>


    <title>
        ${msg("loginTitle",(realm.displayName!''))}
    </title>


    <!-- Favicon -->

    <link
        rel="icon"
        href="${url.resourcesPath}/img/favicon.ico"
    />


    <!-- Common Keycloak CSS -->

    <#if properties.stylesCommon?has_content>

        <#list properties.stylesCommon?split(' ') as style>

            <link
                href="${url.resourcesCommonPath}/${style}"
                rel="stylesheet"
            />

        </#list>

    </#if>


    <!-- Theme CSS -->

    <#if properties.styles?has_content>

        <#list properties.styles?split(' ') as style>

            <link
                href="${url.resourcesPath}/${style}"
                rel="stylesheet"
            />

        </#list>

    </#if>


    <!-- Password visibility -->

    <script
        type="module"
        src="${url.resourcesPath}/js/passwordVisibility.js"
    ></script>


    <!-- Session polling -->

    <script type="module">

        import {
            startSessionPolling
        } from "${url.resourcesPath}/js/authChecker.js";


        startSessionPolling(
            "${url.ssoLoginInOtherTabsUrl?no_esc}"
        );

    </script>


    <!-- Disable double click -->

    <script type="module">

        document.addEventListener("click", (event) => {

            const link =
                event.target.closest("a[data-once-link]");

            if (!link) {
                return;
            }

            if (
                link.getAttribute("aria-disabled")
                === "true"
            ) {

                event.preventDefault();

                return;
            }

            const {
                disabledClass
            } = link.dataset;


            if (disabledClass) {

                link.classList.add(
                    ...disabledClass
                        .trim()
                        .split(/\s+/)
                );

            }


            link.setAttribute(
                "role",
                "link"
            );


            link.setAttribute(
                "aria-disabled",
                "true"
            );

        });

    </script>


    <!-- Authentication session -->

    <#if authenticationSession??>

        <script type="module">

            import {
                checkAuthSession
            } from "${url.resourcesPath}/js/authChecker.js";


            checkAuthSession(
                "${authenticationSession.authSessionIdHash}"
            );

        </script>

    </#if>


    <script>

        // Firefox workaround

        const isFirefox = true;

    </script>

</head>


<body
    id="keycloak-bg"
    class="${properties.kcBodyClass!}"
    data-page-id="login-${pageId}"
>


<div class="cinema-login-page">


    <!-- ====================================================== -->
    <!-- MAIN LOGIN CARD -->
    <!-- ====================================================== -->

    <div class="cinema-login-card">


        <!-- ================================================== -->
        <!-- LEFT : LOGIN FORM -->
        <!-- ================================================== -->

        <section class="cinema-login-left">


            <!-- Logo -->

            <div class="cinema-brand">

                <div class="cinema-logo">
                    🎬
                </div>

                <span>
                    CINEMA
                </span>

            </div>


            <!-- Header -->

            <div class="cinema-login-header">

                <h1 id="kc-page-title">

                    <#nested "header">

                </h1>


                <p class="cinema-login-subtitle">

                    ${msg("loginAccountTitle")}

                </p>

            </div>


            <!-- Language -->

            <#if realm.internationalizationEnabled
                && locale.supported?size gt 1>

                <div class="cinema-language">

                    <select
                        id="login-select-toggle"
                        aria-label="${msg("languages")}"
                        onchange="
                            if (this.value)
                                window.location.href=this.value
                        "
                    >

                        <#list
                            locale.supported?sort_by("label")
                            as l
                        >

                            <option
                                value="${l.url}"
                                ${(l.languageTag == locale.currentLanguageTag)
                                    ?then('selected','')}
                            >

                                ${l.label}

                            </option>

                        </#list>

                    </select>

                </div>

            </#if>


            <!-- ================================================== -->
            <!-- MESSAGE -->
            <!-- ================================================== -->

            <#if
                displayMessage
                && message?has_content
                && (
                    message.type != 'warning'
                    || !isAppInitiatedAction??
                )
            >

                <div
                    id="kc-feedback"
                    class="
                        cinema-alert
                        cinema-alert-${message.type}
                    "
                >

                    <#if message.type == 'success'>

                        ✓

                    <#elseif message.type == 'error'>

                        !

                    <#elseif message.type == 'warning'>

                        ⚠

                    <#else>

                        i

                    </#if>


                    <span>

                        ${kcSanitize(message.summary)?no_esc}

                    </span>

                </div>

            </#if>


            <!-- ================================================== -->
            <!-- FORM -->
            <!-- ================================================== -->

            <div class="cinema-login-form">

                <#nested "form">

            </div>


            <!-- ================================================== -->
            <!-- TRY ANOTHER WAY -->
            <!-- ================================================== -->

            <#if
                auth?has_content
                && auth.showTryAnotherWayLink()
            >

                <form
                    id="kc-select-try-another-way-form"
                    action="${url.loginAction}"
                    method="post"
                    novalidate="novalidate"
                >

                    <input
                        type="hidden"
                        name="tryAnotherWay"
                        value="on"
                    />

                    <a
                        id="try-another-way"
                        href="javascript:document.forms['kc-select-try-another-way-form'].requestSubmit()"
                        class="cinema-secondary-link"
                    >

                        ${kcSanitize(msg("doTryAnotherWay"))?no_esc}

                    </a>

                </form>

            </#if>


            <!-- ================================================== -->
            <!-- SOCIAL LOGIN -->
            <!-- ================================================== -->

            <#if
                realm.password
                && social.providers??
                && social.providers?has_content
            >

                <div class="cinema-divider">

                    <span>
                        ${msg("identity-provider-login-label")}
                    </span>

                </div>


                <div id="kc-social-providers">

                    <#nested "socialProviders">

                </div>

            </#if>


            <!-- ================================================== -->
            <!-- REGISTER -->
            <!-- ================================================== -->

            <#if
                displayInfo
                && realm.password
                && realm.registrationAllowed
                && !registrationDisabled??
            >

                <div class="cinema-register">

                    <#nested "info">

                </div>

            </#if>


            <!-- Footer -->

            <div class="cinema-footer">

                <@loginFooter.content/>

            </div>


        </section>


        <!-- ================================================== -->
        <!-- RIGHT : IMAGE -->
        <!-- ================================================== -->

        <section class="cinema-login-right">


            <div class="cinema-overlay"></div>


            <div class="cinema-hero-content">


                <div class="cinema-hero-badge">

                    PREMIUM CINEMA

                </div>


                <h2>

                    Experience

                    <br>

                    The Magic

                    <br>

                    Of Movies

                </h2>


                <p>

                    Your next cinematic experience
                    starts here.

                </p>


                <div class="cinema-hero-line"></div>


                <div class="cinema-hero-meta">

                    <span>🎬 Movies</span>

                    <span>🍿 Experiences</span>

                    <span>⭐ Premium</span>

                </div>


            </div>


        </section>


    </div>


    <!-- Terms -->

    <div class="cinema-terms">

        By continuing, you agree to our

        <a href="#">
            Terms of Service
        </a>

        and

        <a href="#">
            Privacy Policy
        </a>

    </div>


</div>


</body>

</html>

</#macro>