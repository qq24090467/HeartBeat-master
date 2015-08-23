package com.maxiee.heartbeat.common.tagview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.maxiee.heartbeat.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maxiee on 15-6-16.
 *
 * * Modify from kaedea/Android-Cloud-TagView-Plus (https://github.com/kaedea/Android-Cloud-TagView-Plus)
 *
 */

public class TagView extends RelativeLayout {

    /** tag list */
    private List<Tag> mTags = new ArrayList<Tag>();

    /**
     * System Service
     */
    private Context mContext;
    private ViewTreeObserver mViewTreeObserber;

    /**
     * listener
     */
    private OnTagClickListener mClickListener;
    private OnTagDeleteListener mDeleteListener;

    /** view size param */
    private int mWidth;

    /**
     * layout initialize flag
     */
    private boolean mInitialized = false;

    /**
     * custom layout param
     */
    int lineMargin;
    int tagMargin;
    int textPaddingLeft;
    int textPaddingRight;
    int textPaddingTop;
    int texPaddingBottom;

    public interface OnTagClickListener {
        void onTagClick(Tag tag, int position);
    }

    public interface OnTagDeleteListener {
        void onTagDeleted(Tag tag, int position);
    }

    public TagView(Context ctx) {
        super(ctx, null);
        initialize(ctx, null, 0);
    }

    public TagView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        initialize(ctx, attrs, 0);
    }

    public TagView(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
        initialize(ctx, attrs, defStyle);
    }

    private void initialize(Context ctx, AttributeSet attrs, int defStyle) {
        mContext = ctx;
        mViewTreeObserber = getViewTreeObserver();
        mViewTreeObserber.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!mInitialized) {
                    mInitialized = true;
                    drawTags();
                }
            }
        });

        // get AttributeSet
        TypedArray typeArray = ctx.obtainStyledAttributes(attrs, R.styleable.TagView, defStyle, defStyle);
        this.lineMargin =(int) typeArray.getDimension(R.styleable.TagView_lineMargin, dipToPx(this.getContext(), Constants.DEFAULT_LINE_MARGIN));
        this.tagMargin =(int) typeArray.getDimension(R.styleable.TagView_tagMargin, dipToPx(this.getContext(), Constants.DEFAULT_TAG_MARGIN));
        this.textPaddingLeft =(int) typeArray.getDimension(R.styleable.TagView_textPaddingLeft, dipToPx(this.getContext(), Constants.DEFAULT_TAG_TEXT_PADDING_LEFT));
        this.textPaddingRight =(int) typeArray.getDimension(R.styleable.TagView_textPaddingRight, dipToPx(this.getContext(), Constants.DEFAULT_TAG_TEXT_PADDING_RIGHT));
        this.textPaddingTop =(int) typeArray.getDimension(R.styleable.TagView_textPaddingTop, dipToPx(this.getContext(), Constants.DEFAULT_TAG_TEXT_PADDING_TOP));
        this.texPaddingBottom =(int) typeArray.getDimension(R.styleable.TagView_textPaddingBottom, dipToPx(this.getContext(), Constants.DEFAULT_TAG_TEXT_PADDING_BOTTOM));
        typeArray.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w,h,oldw,oldh);
        mWidth = w;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        if (width<=0)return;
        mWidth=getMeasuredWidth();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawTags();
    }

    /**
     * tag draw
     */
    private void drawTags() {

        if (!mInitialized) {
            return;
        }

        // clear all tag
        removeAllViews();

        // layout padding left & layout padding right
        float total = getPaddingLeft() + getPaddingRight();

        int listIndex = 1;// List Index
        int index_bottom=1;// The Tag to add below
        int index_header=1;// The header tag of this line
        Tag tag_pre = null;
        for (Tag item : mTags) {
            final int position = listIndex-1;
            final Tag tag = item;

            // inflate tag layout
            View tagLayout = (View) LayoutInflater.from(mContext).inflate(R.layout.tagview_item, this, false);
            tagLayout.setId(listIndex);
            tagLayout.setBackground(getSelector(tag));

            // tag text
            TextView tagView = (TextView) tagLayout.findViewById(R.id.tv_tag_item_contain);
            tagView.setText(tag.text);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tagView.getLayoutParams();
            params.setMargins(textPaddingLeft, textPaddingTop, textPaddingRight, texPaddingBottom);
            tagView.setLayoutParams(params);
            tagView.setTextColor(tag.tagTextColor);
            tagView.setTextSize(TypedValue.COMPLEX_UNIT_SP ,tag.tagTextSize);
            tagLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mClickListener != null) {
                        mClickListener.onTagClick(tag, position);
                    }
                }
            });

            // calculate　of tag layout width
            float tagWidth = tagView.getPaint().measureText(tag.text) + textPaddingLeft + textPaddingRight;

            // deletable text
            TextView deletableView = (TextView) tagLayout.findViewById(R.id.tv_tag_item_delete);
            if (tag.isDeletable) {
                deletableView.setVisibility(View.VISIBLE);
                deletableView.setText(tag.deleteIcon);
                int offset = dipToPx(getContext(), 2f);
                deletableView.setPadding(offset, textPaddingTop, textPaddingRight+offset, texPaddingBottom);
                deletableView.setTextColor(tag.deleteIndicatorColor);
                deletableView.setTextSize(TypedValue.COMPLEX_UNIT_SP, tag.deleteIndicatorSize);
                deletableView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TagView.this.remove(position);
                        if (mDeleteListener != null) {
                            mDeleteListener.onTagDeleted(tag, position);
                        }
                    }
                });
                tagWidth += deletableView.getPaint().measureText(tag.deleteIcon) +textPaddingLeft + textPaddingRight;
            } else {
                deletableView.setVisibility(View.GONE);
            }

            if (tag.hasExtraInfo) {
                TextView extraTextView = (TextView) tagLayout.findViewById(R.id.tv_tag_item_extra);
                extraTextView.setVisibility(VISIBLE);
                extraTextView.setText(tag.extraInfoString);
                int offset = dipToPx(getContext(), 2f);
                extraTextView.setPadding(offset, textPaddingTop, textPaddingRight+offset, texPaddingBottom);
                extraTextView.setTextColor(tag.deleteIndicatorColor);
                extraTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, tag.tagTextSize);
                tagWidth += extraTextView.getPaint().measureText(tag.deleteIcon) +textPaddingLeft + textPaddingRight;
            }

            LayoutParams tagParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            //add margin of each line
            tagParams.bottomMargin = lineMargin;

            if (mWidth <= total + tagWidth + dipToPx(this.getContext(), Constants.LAYOUT_WIDTH_OFFSET)) {
                //need to add in new line
                tagParams.addRule(RelativeLayout.BELOW, index_bottom);
                // initialize total param (layout padding left & layout padding right)
                total = getPaddingLeft() + getPaddingRight();
                index_bottom = listIndex;
                index_header =listIndex;
            } else {
                //no need to new line
                tagParams.addRule(RelativeLayout.ALIGN_TOP, index_header);
                //not header of the line
                if (listIndex!=index_header) {
                    tagParams.addRule(RelativeLayout.RIGHT_OF, listIndex - 1);
                    tagParams.leftMargin = tagMargin;
                    total += tagMargin;
                    if (tag_pre.tagTextSize < tag.tagTextSize) {
                        index_bottom = listIndex;
                    }
                }
            }
            total += tagWidth;
            addView(tagLayout, tagParams);
            tag_pre=tag;
            listIndex++;
        }
    }

    private Drawable getSelector(Tag tag) {
        if (tag.background!=null)return tag.background;
        StateListDrawable states = new StateListDrawable();
        GradientDrawable gd_normal = new GradientDrawable();
        gd_normal.setColor(tag.layoutColor);
        gd_normal.setCornerRadius(tag.radius);
        if (tag.layoutBorderSize>0){
            gd_normal.setStroke(dipToPx(getContext(), tag.layoutBorderSize), tag.layoutBorderColor);
        }
        GradientDrawable gd_press = new GradientDrawable();
        gd_press.setColor(tag.layoutColorPress);
        gd_press.setCornerRadius(tag.radius);
        states.addState(new int[] { android.R.attr.state_pressed }, gd_press);
        //must add state_pressed first，or state_pressed will not take effect
        states.addState(new int[] {}, gd_normal);
        return states;
    }





    //public methods
    //----------------- separator  -----------------//

    public boolean isEmpty() {
        return mTags.isEmpty();
    }

    /**
     *
     * @param tag
     */
    public void addTag(Tag tag) {
        mTags.add(tag);
        drawTags();
    }

    public void addTags(String[] tags){
        if (tags==null)return;
        for(String item:tags){
            Tag tag = new Tag(item);
            addTag(tag);
        }
    }

    public void addTags(JSONArray tags) throws JSONException{
        if (tags == null) {
            return;
        }
        int i;
        for (i = 0; i<tags.length(); i++) {
            String tagText = tags.get(i).toString();
            Tag tag = new Tag(tagText);
            addTag(tag);
        }
    }

    public void clear() {
        if (mTags != null) {
            mTags.clear();
        }
    }

    /**
     * get tag list
     *
     * @return mTags TagObject List
     */
    public List<Tag> getTags() {
        return mTags;
    }

    /**
     * remove tag
     *
     * @param position
     */
    public void remove(int position) {
        mTags.remove(position);
        drawTags();
    }




    public int getLineMargin() {
        return lineMargin;
    }

    public void setLineMargin(float lineMargin) {
        this.lineMargin = dipToPx(getContext(), lineMargin);
    }

    public int getTagMargin() {
        return tagMargin;
    }

    public void setTagMargin(float tagMargin) {
        this.tagMargin = dipToPx(getContext(), tagMargin);
    }

    public int getTextPaddingLeft() {
        return textPaddingLeft;
    }

    public void setTextPaddingLeft(float textPaddingLeft) {
        this.textPaddingLeft = dipToPx(getContext(), textPaddingLeft);
    }

    public int getTextPaddingRight() {
        return textPaddingRight;
    }

    public void setTextPaddingRight(float textPaddingRight) {
        this.textPaddingRight = dipToPx(getContext(), textPaddingRight);
    }

    public int getTextPaddingTop() {
        return textPaddingTop;
    }

    public void setTextPaddingTop(float textPaddingTop) {
        this.textPaddingTop = dipToPx(getContext(), textPaddingTop);
    }

    public int getTexPaddingBottom() {
        return texPaddingBottom;
    }

    public void setTexPaddingBottom(float texPaddingBottom) {
        this.texPaddingBottom = dipToPx(getContext(), texPaddingBottom);
    }

    /**
     * setter for OnTagSelectListener
     *
     * @param clickListener
     */
    public void setOnTagClickListener(OnTagClickListener clickListener) {
        mClickListener = clickListener;
    }

    /**
     * setter for OnTagDeleteListener
     *
     * @param deleteListener
     */
    public void setOnTagDeleteListener(OnTagDeleteListener deleteListener) {
        mDeleteListener = deleteListener;
    }

    public static int dipToPx(Context c,float dipValue) {
        DisplayMetrics metrics = c.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    public static int spToPx(Context context, float spValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, metrics);
    }

}
