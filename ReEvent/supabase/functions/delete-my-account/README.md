# delete-my-account

This Function is the only component permitted to use the Supabase service-role key for an account deletion. Android sends its authenticated user JWT through `functions-kt`; it never receives or stores the service-role key.

## Deploy manually

1. Apply migrations through `0011_protected_account_deletion.sql` in order.
2. From the `ReEvent` Android-project directory, link the intended Supabase project.
3. Confirm `SUPABASE_SERVICE_ROLE_KEY` is configured as a Function secret in Supabase. Do not add it to `supabase.local.properties`, BuildConfig, source code, or a client request.
4. Deploy with JWT verification enabled (do **not** use `--no-verify-jwt`):

```powershell
supabase functions deploy delete-my-account
```

5. Use a disposable verified email/password account to run the Android acceptance checklist in `docs/REEVENT_ASSIGNMENT_PROGRESS.md` Module 16. The Function verifies the current password in the same authenticated request and confirms it belongs to the JWT caller before it can use privileged APIs.

The Function removes objects below the user's folder in `resource-photos`, `event-photos`, `partner-logos`, and `profile-avatars`. Add any future private bucket to `PRIVATE_BUCKETS` before enabling deletion for it.
