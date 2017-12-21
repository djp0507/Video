//package com.video.newqu.camera.kmc;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.Color;
//import android.graphics.ColorMatrix;
//import android.graphics.ColorMatrixColorFilter;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ProgressBar;
//import android.widget.RelativeLayout;
//import com.video.newqu.R;
//import com.video.newqu.util.ScreenUtils;
//import com.video.newqu.util.Utils;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * 金山云动态贴纸特效
// */
//
//public class RecyclerViewAdapter extends RecyclerView.Adapter {
//    private static final String TAG = RecyclerViewAdapter.class.getSimpleName();
//    private final int mItemWidth;
//    private List<MaterialInfoItem> mImageList;
//    private Context mContext;
//    private int selectIndex = -1;
//    private RecyclerView mRecyclerView;
//
//    public static int STATE_INIT = 0;
//    public static int STATE_DOWNLOADING = 1;
//    public static int STATE_DOWNLOADED = 2;
//    public static int STATE_COOLDOWNING = 3;
//    public static int STATE_STICKER_READY = 4;
//    public static int STATE_DOWNLOADTHUMBNAIL = 5;
//
//    private Map<Integer, Integer> mItemState;
//
//    public interface OnRecyclerViewListener {
//        void onItemClick(int position);
//
//        boolean onItemLongClick(int position);
//    }
//
//    private OnRecyclerViewListener onRecyclerViewListener;
//
//    public void setOnItemClickListener(OnRecyclerViewListener onRecyclerViewListener) {
//        this.onRecyclerViewListener = onRecyclerViewListener;
//    }
//
//
//    public RecyclerViewAdapter(List<MaterialInfoItem> materialList, Context context) {
//        this.mContext = context;
//        this.mImageList = materialList;
//        int count = getItemCount();
//        mItemWidth = (ScreenUtils.getScreenWidth()- Utils.dip2px(80))/5;
//        mItemState = new HashMap<Integer, Integer>(count);
//        for (int i = 0; i < mImageList.size(); i++) {
//            mItemState.put(i, STATE_INIT);
//        }
//    }
//
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sticker_list_item, null);
//        return new MaterialViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
//        final MaterialViewHolder holder = (MaterialViewHolder) viewHolder;
//        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.fl_item.getLayoutParams();
//        layoutParams.width= FrameLayout.LayoutParams.MATCH_PARENT;
//        layoutParams.height=mItemWidth;
//        holder.fl_item.setLayoutParams(layoutParams);
//
//        updateItemView(holder, position);
//        holder.position = position;
//
//        if (mItemState.get(position) == null) {
//            return;
//        }
//
//        if (mItemState.get(position) == STATE_STICKER_READY ||
//                (position == 0) || mItemState.get(position) == STATE_INIT) {
//            if (position == selectIndex) {
//                viewHolder.itemView.setSelected(true);
//                holder.mImage.setBackgroundResource(R.drawable.sticker_item_chosen);
//            }
//        }
//
//        if (position != selectIndex) {
//            viewHolder.itemView.setSelected(false);
//            holder.mImage.setBackgroundColor(Color.TRANSPARENT);
//        }
//
//        if (position == 0) {
//            holder.mDLIndImage.setVisibility(View.GONE);
//        } else {
//            if (mImageList.get(position).hasDownload) {
//                Log.d(TAG, "position " + position + " have downloaded");
//                holder.mDLIndImage.setVisibility(View.GONE);
//                holder.mDownloadProgress.setVisibility(View.GONE);
//                mItemState.put(position, STATE_STICKER_READY);
//            }
//        }
//
//        holder.mImage.setImageBitmap(mImageList.get(position).thumbnail);
//    }
//
//    @Override
//    public int getItemCount() {
//        if (mImageList == null) {
//            return 0;
//        }
//        return mImageList.size();
//    }
//
//    public void setRecyclerView(RecyclerView view) {
//        this.mRecyclerView = view;
//    }
//
//    class MaterialViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
//            View.OnLongClickListener {
//        private ImageView mImage;
//        private ImageView mDLIndImage;
//        private ProgressBar mDownloadProgress;
//        private CircleProgressBar mCoolDownProgressBar;
//        private int position;
//        private FrameLayout fl_item;
//
//        public MaterialViewHolder(View itemView) {
//            super(itemView);
//            this.mImage = (ImageView) itemView.findViewById(R.id.gimg_list_item);
//            this.mDLIndImage = (ImageView) itemView.findViewById(R.id.download_ind);
//            this.mDownloadProgress = (ProgressBar) itemView.findViewById(R.id.process_download);
//            this.mCoolDownProgressBar = (CircleProgressBar) itemView.findViewById(R.id.circle_progressbar);
//            this.fl_item = (FrameLayout) itemView.findViewById(R.id.fl_item);
//
//            itemView.setOnClickListener(this);
//            itemView.setOnLongClickListener(this);
//        }
//
//        @Override
//        public void onClick(View v) {
//            if (null != onRecyclerViewListener) {
//                onRecyclerViewListener.onItemClick(position);
//            }
//        }
//
//        @Override
//        public boolean onLongClick(View v) {
//            if (null != onRecyclerViewListener) {
//                return onRecyclerViewListener.onItemLongClick(position);
//            }
//            return false;
//        }
//    }
//
//    public boolean isCoolDowning(int position) {
//        if (mItemState == null || mItemState.size() <= position) {
//            return true;
//        }
//        return mItemState.get(position) == STATE_COOLDOWNING ? true : false;
//    }
//
//    public void triggerCoolDown(int position) {
//        Log.d(TAG, "triggerCoolDown for position " + position);
//    }
//
//    public void updateItemView(int position) {
//
//        LinearLayoutManager layoutManager = ((LinearLayoutManager) mRecyclerView.getLayoutManager());
//        int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
//        int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
//
//        int state = mItemState.get(position);
//
//        if (position >= firstVisiblePosition && position <= lastVisiblePosition) {
//            if (state == STATE_DOWNLOADING) {
//                View view = mRecyclerView.getChildAt(position - firstVisiblePosition);
//                ProgressBar process = (ProgressBar) view.findViewById(R.id.process_download);
//                ImageView thumbImage = (ImageView) view.findViewById(R.id.gimg_list_item);
//                changeLight(thumbImage, -100);
//                process.setVisibility(View.VISIBLE);
//            } else if (state == STATE_DOWNLOADED) {
//                View view = mRecyclerView.getChildAt(position - firstVisiblePosition);
//                ImageView dlIndImage = (ImageView) view.findViewById(R.id.download_ind);
//                ProgressBar process = (ProgressBar) view.findViewById(R.id.process_download);
//                ImageView thumbImage = (ImageView) view.findViewById(R.id.gimg_list_item);
//                changeLight(thumbImage, 0);
//                process.setVisibility(View.GONE);
//                dlIndImage.setVisibility(View.GONE);
//            }
//            if (state == STATE_DOWNLOADTHUMBNAIL) {
//                View view = mRecyclerView.getChildAt(position
//                        - firstVisiblePosition);
//                ImageView thumbImage = (ImageView) view.findViewById(R.id.gimg_list_item);
//                changeLight(thumbImage, -100);
//            }
//        }
//    }
//
//    public void updateItemView(MaterialViewHolder holder, int position) {
//        if (mItemState.get(position) == null) {
//            return;
//        }
//        int state = mItemState.get(position);
//        Log.d(TAG, "updateItemView with holder for position " + position + " ,the state is " + state);
//
//        if (state == STATE_DOWNLOADING) {
//            Log.d(TAG, "updateItemView with holder at state STATE_DOWNLOADING");
//            holder.mDownloadProgress.setVisibility(View.VISIBLE);
//            holder.mDLIndImage.setVisibility(View.VISIBLE);
//            changeLight(holder.mImage, -100);
//        } else if (state == STATE_DOWNLOADED) {
//            Log.d(TAG, "updateItemView with holder at state STATE_DOWNLOADED");
//            holder.mDownloadProgress.setVisibility(View.GONE);
//            changeLight(holder.mImage, 0);
//            holder.mDLIndImage.setVisibility(View.GONE);
//        } else {
//            Log.d(TAG, "updateItemView with holder");
//            holder.mDownloadProgress.setVisibility(View.GONE);
//            changeLight(holder.mImage, 0);
//            if (mImageList.get(position).hasDownload) {
//                holder.mDLIndImage.setVisibility(View.GONE);
//            } else {
//                holder.mDLIndImage.setVisibility(View.VISIBLE);
//            }
//        }
//    }
//
//    private void changeLight(ImageView imageview, int brightness) {
//        ColorMatrix matrix = new ColorMatrix();
//        matrix.set(new float[]{1, 0, 0, 0, brightness, 0, 1, 0, 0,
//                brightness, 0, 0, 1, 0, brightness, 0, 0, 0, 1, 0});
//        imageview.setColorFilter(new ColorMatrixColorFilter(matrix));
//
//    }
//
//    public void setSelectIndex(int i) {
//        selectIndex = i;
//        notifyDataSetChanged();
//    }
//
//    public void setItemState(int position, int state) {
//        mItemState.put(position, state);
//    }
//
//    public int getItemState(int position) {
//        return mItemState.get(position);
//    }
//}
