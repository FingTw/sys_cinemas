<#import "template.ftl" as layout>
<#import "field.ftl" as field>
<#import "buttons.ftl" as buttons>
<#import "social-providers.ftl" as identityProviders>


<@layout.registrationLayout
    displayMessage=!messagesPerField.existsError('username','password')
    displayInfo=
        realm.password
        && realm.registrationAllowed
        && !registrationDisabled??
    ; section
>


    <!-- ====================================================== -->
    <!-- HEADER -->
    <!-- ====================================================== -->

    <#if section = "header">

        ${msg("loginAccountTitle")}


    <!-- ====================================================== -->
    <!-- LOGIN FORM -->
    <!-- ====================================================== -->

    <#elseif section = "form">


        <#if realm.password>


            <form
                id="kc-form-login"
                class="${properties.kcFormClass!}"
                onsubmit="login.disabled = true; return true;"
                action="${url.loginAction}"
                method="post"
                novalidate="novalidate"
            >


                <!-- USERNAME -->

                <#if !usernameHidden??>


                    <#assign label>

                        <#if !realm.loginWithEmailAllowed>

                            ${msg("username")}

                        <#elseif !realm.registrationEmailAsUsername>

                            ${msg("usernameOrEmail")}

                        <#else>

                            ${msg("email")}

                        </#if>

                    </#assign>


                    <div class="cinema-field">


                        <label
                            for="username"
                            class="cinema-label"
                        >

                            ${label}

                        </label>


                        <input
                            id="username"
                            name="username"
                            value="${login.username!''}"
                            type="text"
                            autofocus
                            autocomplete="username"
                            class="cinema-input"
                            placeholder="<#if realm.loginWithEmailAllowed>${msg("email")}<#else>${msg("username")}</#if>"
                        />


                        <#if messagesPerField.existsError('username')>

                            <div class="cinema-field-error">

                                ${kcSanitize(
                                    messagesPerField
                                    .getFirstError('username')
                                )?no_esc}

                            </div>

                        </#if>


                    </div>


                <#else>


                    <input
                        type="hidden"
                        name="username"
                        value="${login.username!''}"
                    />


                </#if>



                <!-- PASSWORD -->

                <div class="cinema-field">


                    <div class="cinema-label-row">

                        <label
                            for="password"
                            class="cinema-label"
                        >

                            ${msg("password")}

                        </label>


                        <#if realm.resetPasswordAllowed>

                            <a
                                href="${url.loginResetCredentialsUrl}"
                                class="cinema-forgot"
                            >

                                ${msg("doForgotPassword")}

                            </a>

                        </#if>


                    </div>


                    <div class="cinema-password-wrapper">


                        <input
                            id="password"
                            name="password"
                            type="password"
                            autocomplete="current-password"
                            class="cinema-input cinema-password-input"
                            placeholder="${msg("password")}"
                        />


                        <button
                            type="button"
                            class="cinema-password-toggle"
                            onclick="toggleCinemaPassword()"
                            aria-label="Show password"
                        >

                            👁

                        </button>


                    </div>


                    <#if messagesPerField.existsError('password')>

                        <div class="cinema-field-error">

                            ${kcSanitize(
                                messagesPerField
                                .getFirstError('password')
                            )?no_esc}

                        </div>

                    </#if>


                </div>



                <!-- REMEMBER ME -->

                <#if realm.rememberMe && !usernameHidden??>

                    <div class="cinema-options">


                        <label class="cinema-checkbox">


                            <input
                                type="checkbox"
                                id="rememberMe"
                                name="rememberMe"
                                <#if login.rememberMe??>
                                    checked
                                </#if>
                            />


                            <span class="cinema-checkmark"></span>


                            <span>

                                ${msg("rememberMe")}

                            </span>


                        </label>


                    </div>

                </#if>



                <!-- HIDDEN CREDENTIAL ID -->

                <input
                    type="hidden"
                    id="id-hidden-input"
                    name="credentialId"
                    <#if auth.selectedCredential?has_content>
                        value="${auth.selectedCredential}"
                    </#if>
                />


                <!-- LOGIN BUTTON -->

                <button
                    id="kc-login"
                    name="login"
                    type="submit"
                    class="cinema-login-button"
                >

                    <span>

                        ${msg("doLogIn")}

                    </span>

                    <span class="cinema-button-arrow">

                        →

                    </span>

                </button>


            </form>


        </#if>



    <!-- ====================================================== -->
    <!-- SOCIAL PROVIDERS -->
    <!-- ====================================================== -->

    <#elseif section = "socialProviders">


        <#if
            realm.password
            && social.providers??
            && social.providers?has_content
        >

            <div class="cinema-social-list">

                <@identityProviders.show
                    social=social
                />

            </div>

        </#if>



    <!-- ====================================================== -->
    <!-- REGISTER -->
    <!-- ====================================================== -->

    <#elseif section = "info">


        <#if
            realm.password
            && realm.registrationAllowed
            && !registrationDisabled??
        >

            <div class="cinema-register-text">

                <span>

                    ${msg("noAccount")}

                </span>


                <a
                    href="${url.registrationUrl}"
                >

                    ${msg("doRegister")}

                </a>

            </div>

        </#if>


    </#if>


</@layout.registrationLayout>



<script>

function toggleCinemaPassword() {

    const password =
        document.getElementById("password");

    const button =
        document.querySelector(
            ".cinema-password-toggle"
        );


    if (!password) {

        return;

    }


    if (password.type === "password") {

        password.type = "text";

        button.textContent = "🙈";

    } else {

        password.type = "password";

        button.textContent = "👁";

    }

}

</script>