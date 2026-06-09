-- Index on user_roles for role queries
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON auth.user_roles(role_id);

-- Index on user_permissions for permission lookups
CREATE INDEX IF NOT EXISTS idx_user_permissions_permission_id ON auth.user_permissions(permission_id);

-- Index on role_permissions for permission mapping
CREATE INDEX IF NOT EXISTS idx_role_permissions_permission_id ON auth.role_permissions(permission_id);

-- Index for active tokens (checking sessions / checking token validity)
CREATE INDEX IF NOT EXISTS idx_auth_tokens_active_deleted ON auth.auth_tokens(is_active, is_deleted);
