use crate::{RemotePushExt, Result};
use tauri::{command, AppHandle, Runtime};

#[command]
pub(crate) async fn get_token<R: Runtime>(app: AppHandle<R>) -> Result<String> {
    let token = app.remote_push().get_token()?;
    Ok(token.token)
}

#[command]
pub(crate) fn request_permission<R: Runtime>(app: AppHandle<R>) -> Result<()> {
    app.remote_push().request_permission()
}
