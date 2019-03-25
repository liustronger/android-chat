package cn.wildfire.chat.app.main;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import butterknife.Bind;
import butterknife.OnClick;
import cn.wildfire.chat.app.Config;
import cn.wildfire.chat.app.login.model.PCSession;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.net.OKHttpHelper;
import cn.wildfire.chat.kit.net.SimpleCallback;
import cn.wildfire.chat.kit.user.UserViewModel;
import cn.wildfirechat.chat.R;

public class PCLoginActivity extends WfcBaseActivity {
    private String token;
    private PCSession pcSession;
    @Bind(R.id.confirmButton)
    Button confirmButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        token = getIntent().getStringExtra("token");
        if (TextUtils.isEmpty(token)) {
            finish();
        }
    }

    @Override
    protected int contentLayout() {
        return R.layout.pc_login_activity;
    }

    @Override
    protected void afterViews() {
        scanPCLogin(token);
    }

    @OnClick(R.id.confirmButton)
    void confirmPCLogin() {
        if (System.currentTimeMillis() > this.pcSession.getExpired()) {
            Toast.makeText(PCLoginActivity.this, "已过期", Toast.LENGTH_SHORT).show();
        } else {
            UserViewModel userViewModel = ViewModelProviders.of(PCLoginActivity.this).get(UserViewModel.class);
            confirmPCLogin(token, userViewModel.getUserId());
        }
    }

    private void scanPCLogin(String token) {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .content("处理中")
                .progress(true, 100)
                .build();
        dialog.show();
        String url = "http://" + Config.IM_SERVER_HOST + ":" + Config.IM_SERVER_PORT + "/api/scan_pc";
        url += "/" + token;
        OKHttpHelper.get(url, null, new SimpleCallback<PCSession>() {
            @Override
            public void onUiSuccess(PCSession pcSession) {
                if (isFinishing()) {
                    return;
                }
                dialog.dismiss();
                PCLoginActivity.this.pcSession = pcSession;
                if (pcSession.getStatus() == 0) {
                    confirmButton.setEnabled(true);
                } else {
                    Toast.makeText(PCLoginActivity.this, "status: " + pcSession.getStatus(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onUiFailure(int code, String msg) {
                if (isFinishing()) {
                    return;
                }
                Toast.makeText(PCLoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void confirmPCLogin(String token, String userId) {
        String url = "http://" + Config.IM_SERVER_HOST + ":" + Config.IM_SERVER_PORT + "/api/confirm_pc";

        Map<String, String> params = new HashMap<>(2);
        params.put("user_id", userId);
        params.put("token", token);
        OKHttpHelper.post(url, params, new SimpleCallback<PCSession>() {
            @Override
            public void onUiSuccess(PCSession pcSession) {
                if (isFinishing()) {
                    return;
                }
                Toast.makeText(PCLoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onUiFailure(int code, String msg) {
                if (isFinishing()) {
                    return;
                }
                Toast.makeText(PCLoginActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
