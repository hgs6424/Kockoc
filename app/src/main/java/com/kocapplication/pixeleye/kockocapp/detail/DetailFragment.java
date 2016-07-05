package com.kocapplication.pixeleye.kockocapp.detail;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.kocapplication.pixeleye.kockocapp.R;
import com.kocapplication.pixeleye.kockocapp.model.Course;
import com.kocapplication.pixeleye.kockocapp.user.UserActivity;
import com.kocapplication.pixeleye.kockocapp.util.BasicValue;
import com.kocapplication.pixeleye.kockocapp.util.JspConn;

import org.apmem.tools.layouts.FlowLayout;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by hp on 2016-06-21.
 */
public class DetailFragment extends Fragment {
    final static String TAG = "DetailFragment";
    DetailPageData detailPageData;
    DetailRecyclerAdapter adapter;

    LinearLayout ll_profile;
    LinearLayout ll_htmlInfo;
    LinearLayout ll_board_img;
    LinearLayout ll_board_map;
    LinearLayout ll_comment_menu;
    LinearLayout ll_bgLayout;
    FlowLayout fl_board_hashtag;
    RecyclerView rv_comment_list;
    RecyclerView course_recyclerView;
    DetailCourseAdapter course_adapter;

    ToggleButton btn_like;
    Spinner course_spinner;

    TextView course_title;
    TextView profile_nickname;
    TextView profile_date;
    TextView board_text;
    TextView html_title;
    TextView html_desc;
    TextView comment_scrap;
    TextView comment_count;
    TextView comment_link;

    ImageView profile_img;
    ImageView html_img;
    ImageView board_mainimg;
//    ImageView board_courses;

    private int boardNo;
    private int courseNo;
    private int board_userNo;
    LayoutInflater mInflater;

    public DetailFragment() {
        super();
    }

    @SuppressLint("ValidFragment")
    public DetailFragment(int boardNo, int courseNo, int board_userNo) {
        this.boardNo = boardNo;
        this.courseNo = courseNo;
        this.board_userNo = board_userNo;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.detail_content, container, false);
        this.mInflater = inflater;

        init(view);

        //DetailThread 에서 데이터를 받아옴
        Handler handler = new DetailDataReceiveHandler();
        Thread thread = new DetailThread(handler, boardNo, courseNo);
        thread.start();

        return view;
    }

    private void init(View view) {
        detailPageData = new DetailPageData();

        ll_profile = (LinearLayout) view.findViewById(R.id.ll_profile);
        ll_htmlInfo = (LinearLayout) view.findViewById(R.id.ll_htmlInfo);
        ll_board_img = (LinearLayout) view.findViewById(R.id.ll_detail_content_imgViewList);
        ll_board_map = (LinearLayout) view.findViewById(R.id.ll_detail_content_maps);
        ll_comment_menu = (LinearLayout) view.findViewById(R.id.ll_comment_menu);
        ll_bgLayout = (LinearLayout) view.findViewById(R.id.ll_bg_detail_up);

        btn_like = (ToggleButton) view.findViewById(R.id.toggle_detail_content_like);
        course_spinner = (Spinner) view.findViewById(R.id.course_spinner);
        course_title = (TextView) view.findViewById(R.id.course_title);
        profile_nickname = (TextView) view.findViewById(R.id.tv_detail_inner_up_nickname);
        profile_date = (TextView) view.findViewById(R.id.tv_detail_inner_up_date);
        board_text = (TextView) view.findViewById(R.id.tv_detail_content_text);
        html_title = (TextView) view.findViewById(R.id.tv_detail_content_htmlTitle);
        html_desc = (TextView) view.findViewById(R.id.tv_detail_content_htmlDesc);
        comment_scrap = (TextView) view.findViewById(R.id.tv_detail_comment_scrap);
        comment_count = (TextView) view.findViewById(R.id.tv_detail_comment_count);
        comment_link = (TextView) view.findViewById(R.id.tv_detail_comment_link);
        profile_img = (ImageView) view.findViewById(R.id.img_detail_inner_up_profile);
        html_img = (ImageView) view.findViewById(R.id.tv_detail_content_htmlImg);
        board_mainimg = (ImageView) view.findViewById(R.id.img_detail_content_main_img);
//        board_courses = (ImageView) view.findViewById(R.id.iv_detail_content_courses);
        fl_board_hashtag = (FlowLayout) view.findViewById(R.id.fl_detail_content_tag);
        View includeView = view.findViewById(R.id.detail_commentlist_layout);

        setCourseRecyclerView(view);

        rv_comment_list = (RecyclerView) includeView.findViewById(R.id.rv_detail_commentlist);

        ll_profile.setOnClickListener(new ProfileClickListener());

        btn_like.setOnClickListener(new LikeClickListener());
    }

    private void setCourseRecyclerView(View view) {
        course_recyclerView = (RecyclerView) view.findViewById(R.id.iv_detail_content_courses);
        course_adapter = new DetailCourseAdapter(new ArrayList<Course>());
        course_recyclerView.setAdapter(course_adapter);

        LinearLayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        manager.scrollToPosition(0);
        course_recyclerView.setLayoutManager(manager);

        course_recyclerView.setHasFixedSize(true);

    }

    private void setData(DetailPageData data) {
        profile_nickname.setText(data.getUserName());
        profile_date.setText(data.getBoardDate());
        board_text.setText(data.getBoardText());
        comment_scrap.setText(String.valueOf(detailPageData.getScrapNumber()));
        comment_count.setText(String.valueOf(detailPageData.getCommentArr().size()));

        //해시태그
        for (int l = 0; l < data.getHashTagArr().size(); l++) {
            TextView textTemp = new TextView(getActivity());
            textTemp.setTextColor(mInflater.getContext().getResources().getColor(R.color.innerTagColor));
            textTemp.setText(data.getHashTagArr().get(l));
            fl_board_hashtag.addView(textTemp);
        }
    }

    private void setImg(DetailPageData data) {
        //프로필 이미지
        Glide.with(getActivity()).load(BasicValue.getInstance().getUrlHead() + "board_image/" + data.getUserNo() + "/profile.jpg").into(profile_img);

        //게시글 이미지
        for (int i = 0; i < data.getBoardImgArr().size(); i++) {
            //ImageView 생성
            ImageView temp = new ImageView(getActivity());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = 10;
            temp.setLayoutParams(params);
            temp.setAdjustViewBounds(true);
            temp.setScaleType(ImageView.ScaleType.FIT_CENTER);

            Glide.with(getActivity()).load(BasicValue.getInstance().getUrlHead() + "board_image/" + data.getUserNo() + "/" + data.getBoardImgArr().get(i)).into(temp);
            ll_board_img.addView(temp);
        }
    }

    /**
     * setCommentList
     * 댓글 데이터를 RecyclerView에 붙임
     */
    private void setCommentList() {
        adapter = new DetailRecyclerAdapter(detailPageData.getCommentArr(), getActivity(), new CommentClickListener());
        rv_comment_list.setAdapter(adapter);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rv_comment_list.setLayoutManager(manager);
        rv_comment_list.setHasFixedSize(true);
    }

    /**
     * addComment
     * 댓글을 달면 데이터를 새로 받아 어댑터를 다시 붙임
     */
    public void addComment() {
        Handler handler = new RefreshDataReceiveHandler();
        Thread thread = new DetailThread(handler, boardNo, courseNo);
        thread.start();
    }

    private class DetailDataReceiveHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            detailPageData = (DetailPageData) msg.getData().getSerializable("THREAD");
            setData(detailPageData);
            setImg(detailPageData);
            setCommentList();
            //DetailData를 받아 오면 좋아요 수 얻어옴
            Handler ex_handler = new ExpressionCheckHandler();
            Thread ex_thread = new ExpressionCheckThread(ex_handler, boardNo);
            ex_thread.start();
        }
    }

    private class RefreshDataReceiveHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            detailPageData = (DetailPageData) msg.getData().getSerializable("THREAD");
            adapter = new DetailRecyclerAdapter(detailPageData.getCommentArr(), getActivity(), new CommentClickListener());
            rv_comment_list.setAdapter(adapter);
        }
    }

    /**
     * ExpressionCheckHandler
     * 좋아요 값 표시
     */
    private class ExpressionCheckHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String receive = msg.getData().getString("MESSAGE");

            btn_like.setTextOff(detailPageData.getRecommend_No() + "");
            btn_like.setTextOn((detailPageData.getRecommend_No() + 1) + "");
            btn_like.setText(detailPageData.getRecommend_No() + "");

            try {
                JSONObject upperObj = new JSONObject(receive);
                JSONArray array = upperObj.getJSONArray("userArr");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    if (BasicValue.getInstance().getUserNo() == object.getInt("userNo")) {
                        btn_like.setChecked(true);
                        btn_like.setTextOn(detailPageData.getRecommend_No() + "");
                        btn_like.setTextOff((detailPageData.getRecommend_No() - 1) + "");
                        btn_like.setText(detailPageData.getRecommend_No() + "");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class ProfileClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent user_intent = new Intent(getActivity(), UserActivity.class);
            user_intent.putExtra("userNo", board_userNo);
            startActivity(user_intent);
        }
    }

    private class LikeClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            JspConn.writeExpression(detailPageData.getBoardNo(), 0);
        }
    }

    private class CommentClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final int position = rv_comment_list.getChildLayoutPosition(v);
            try {
                if (detailPageData.getCommentArr().get(position).getComment_userNo() == BasicValue.getInstance().getUserNo()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setTitle("안내");
                    builder.setMessage("삭제하시겠습니까?");
                    builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            JspConn.DeleteComment(detailPageData.getCommentArr().get(position).getComment_No());
                            adapter.removeItem(position);
                        }
                    });
                    builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.create().show();
                }
            } catch (Exception e) {
                Log.e(TAG, "댓글 삭제 오류" + e.getMessage());
            }
        }
    }
}
