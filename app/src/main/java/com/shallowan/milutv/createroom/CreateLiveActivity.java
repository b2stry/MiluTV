package com.shallowan.milutv.createroom;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.sdsmdg.tastytoast.TastyToast;
import com.shallowan.milutv.MiluApplication;
import com.shallowan.milutv.R;
import com.shallowan.milutv.hostlive.HostLiveActivity;
import com.shallowan.milutv.model.RoomInfo;
import com.shallowan.milutv.utils.ImgUtils;
import com.shallowan.milutv.utils.PicChooserHelper;
import com.shallowan.milutv.utils.request.BaseRequest;
import com.tencent.TIMUserProfile;

/**
 * Created by ShallowAn.
 */

public class CreateLiveActivity extends AppCompatActivity {

    public View mSetCoverView;

    public ImageView mCoverImg;

    public TextView mCoverTipTxt;

    public EditText mTitleEt;

    public TextView mCreateRoomBtn;

    public TextView mRoomNoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        findAllViews();
        setListeners();
        setupTitlebar();
    }

    private void findAllViews() {
        mSetCoverView = findViewById(R.id.set_cover);
        mCoverImg = (ImageView) findViewById(R.id.cover);
        mCoverTipTxt = (TextView) findViewById(R.id.tv_pic_tip);
        mTitleEt = (EditText) findViewById(R.id.title);
        mCreateRoomBtn = (TextView) findViewById(R.id.create);
        mRoomNoText = (TextView) findViewById(R.id.room_no);
    }


    private void setListeners() {
        mSetCoverView.setOnClickListener(clickListener);
        mCreateRoomBtn.setOnClickListener(clickListener);
    }

    private void setupTitlebar() {
        Toolbar titlebar = (Toolbar) findViewById(R.id.titlebar);
        titlebar.setTitle("开始我的直播");
        titlebar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(titlebar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        titlebar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.create) {
                //创建直播
                if (mTitleEt.getText().toString().equals("")) {
                    TastyToast.makeText(getApplicationContext(), "请输入一个标题！", TastyToast.LENGTH_LONG, TastyToast.WARNING);

                } else {
                    requestCreateRoom();
                }
            } else if (id == R.id.set_cover) {
                //选择图片
                choosePic();
            }
        }
    };

    private void requestCreateRoom() {
        CreateRoomRequest.CreateRoomParam param = new CreateRoomRequest.CreateRoomParam();
        TIMUserProfile selfProfile = MiluApplication.getApplication().getSelfProfile();
        param.userId = selfProfile.getIdentifier();
        param.userAvatar = selfProfile.getFaceUrl();
        String nickName = selfProfile.getNickName();
        param.userName = TextUtils.isEmpty(nickName) ? selfProfile.getIdentifier() : nickName;
        param.liveTitle = mTitleEt.getText().toString();
        param.liveCover = coverUrl;
        //创建房间
        CreateRoomRequest request = new CreateRoomRequest();
        request.setOnResultListener(new BaseRequest.OnResultListener<RoomInfo>() {
            @Override
            public void onFail(int code, String msg) {
                TastyToast.makeText(getApplicationContext(), "请求失败：" + msg, TastyToast.LENGTH_LONG, TastyToast.ERROR);

            }

            @Override
            public void onSuccess(RoomInfo roomInfo) {
                Intent intent = new Intent();
                intent.setClass(CreateLiveActivity.this, HostLiveActivity.class);
                intent.putExtra("roomId", roomInfo.roomId);
                Log.i("roomid", roomInfo.roomId + "");
                startActivity(intent);
                finish();
            }
        });


        String requestUrl = request.getUrl(param);
        request.request(requestUrl);
    }


    private PicChooserHelper mPicChooserHelper;

    private void choosePic() {
        if (mPicChooserHelper == null) {
            mPicChooserHelper = new PicChooserHelper(this, PicChooserHelper.PicType.Cover);
            mPicChooserHelper.setOnChooseResultListener(new PicChooserHelper.OnChooseResultListener() {
                @Override
                public void onSuccess(String url) {
                    //获取图片成功
                    updateCover(url);
                }

                @Override
                public void onFail(String msg) {
                    //获取图片失败
                    TastyToast.makeText(getApplicationContext(), "选择失败：" + msg, TastyToast.LENGTH_LONG, TastyToast.ERROR);

                }
            });
        }
        mPicChooserHelper.showPicChooserDialog();
    }

    private String coverUrl = null;

    private void updateCover(String url) {
        coverUrl = url;
        ImgUtils.load(url, mCoverImg);
        mCoverTipTxt.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mPicChooserHelper != null) {
            mPicChooserHelper.onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


}