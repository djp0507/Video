package com.video.newqu.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.video.newqu.R;

/**
 * 这个控件只做上拉加载更多
 */
public class PullToLoadMore extends ListView implements AbsListView.OnScrollListener {

	private static final String TAG = "RefreshListView";
	private final Context context;
	private int firstVisibleItemPosition; // 屏幕显示在第一个的item的索引
	private int downY; // 按下时y轴的偏移量
	private final int DOWN_PULL_REFRESH = 0; // 下拉刷新状态
	private final int RELEASE_REFRESH = 1; // 松开刷新
	private final int REFRESHING = 2; // 正在刷新中
	private int currentState = DOWN_PULL_REFRESH; // 头布局的状态: 默认为下拉刷新状态
	private OnLoadMoreListener mOnLoadMoreListener;
	private boolean isScrollToBottom; // 是否滑动到底部
	private boolean isLoadingMore = false; // 是否正在加载更多中
	private View footerView; // 脚布局的对象
	private int footerViewHeight; // 脚布局的高度
	private TextView mFooterState;
	private ProgressBar mProgressBar;

	public interface OnLoadMoreListener{
		/**
		 * 上拉加载更多
		 */
		void onLoadingMore();
	}

	public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
		mOnLoadMoreListener = onLoadMoreListener;
	}

	public PullToLoadMore(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context=context;
		addFooterView();
		this.setOnScrollListener(this);
	}

	/**
	 * 初始化脚布局
	 */
	private void addFooterView() {
		footerView = View.inflate(context, R.layout.list_foot_loading, null);
		footerView.measure(0, 0);
		footerViewHeight = footerView.getMeasuredHeight();
		footerView.setPadding(0, -footerViewHeight, 0, 0);
		this.addFooterView(footerView);
		mFooterState = ((TextView) findViewById(R.id.foot_state));
		mProgressBar = ((ProgressBar) findViewById(R.id.probar));
	}
	/**
	 * 当滚动状态改变时回调
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

		if (scrollState == SCROLL_STATE_IDLE
				|| scrollState == SCROLL_STATE_FLING) {
			// 判断当前是否已经到了底部
			if (isScrollToBottom && !isLoadingMore) {
				isLoadingMore = true;
				// 当前到底部
				Log.i(TAG, "加载更多数据");
				footerView.setPadding(0, 0, 0, 0);
				mProgressBar.setVisibility(View.VISIBLE);
				mFooterState.setText("加载中，请稍后...");//正在获取更多资源
				this.setSelection(this.getCount());

				if (mOnLoadMoreListener != null) {
					mOnLoadMoreListener.onLoadingMore();
				}
			}
		}
	}

	/**
	 * 当滚动时调用
	 *
	 * @param firstVisibleItem
	 *            当前屏幕显示在顶部的item的position
	 * @param visibleItemCount
	 *            当前屏幕显示了多少个条目的总数
	 * @param totalItemCount
	 *            ListView的总条目的总数
	 */
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
						 int visibleItemCount, int totalItemCount) {
		firstVisibleItemPosition = firstVisibleItem;

		if (getLastVisiblePosition() == (totalItemCount - 1)) {
			isScrollToBottom = true;
		} else {
			isScrollToBottom = false;
		}
	}


	/**
	 * 隐藏脚布局
	 */
	public void hideFooterView() {
		mProgressBar.clearAnimation();
		mProgressBar.setVisibility(View.INVISIBLE);
		footerView.setPadding(0, -footerViewHeight, 0, 0);
		mFooterState.setText("");
		isLoadingMore = false;
	}
}