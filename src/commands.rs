use tauri::{command, AppHandle, Runtime};

use crate::{RemotePushExt, Result};

#[command]
pub(crate) async fn get_token<R: Runtime>(app: AppHandle<R>) -> Result<String> {
    app.remote_push().get_token()
}

#[command]
pub(crate) async fn request_permission<R: Runtime>(
    app: AppHandle<R>,
) -> Result<crate::models::PermissionState> {
    app.remote_push().request_permission().await
}
