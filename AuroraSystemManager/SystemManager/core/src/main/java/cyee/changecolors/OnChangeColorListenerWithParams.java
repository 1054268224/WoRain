package cyee.changecolors;
// Gionee <zhaoyulong> <2015-05-13> add for CR01480269 begin
/**
 * 变色龙变色接口
 * @author <a href="mailto:zhaoyulong@gionee.com">赵玉龙</a>
 */
public interface OnChangeColorListenerWithParams {
	/**
	 * 变色回调函数
	 * @param changeColorType {@link ColorConfigConstants.TYPE_DEFAULT_CHANGE_COLOR}
	 * 和{@link ColorConfigConstants.TYPE_THEME_CHANGE_COLOR}之一
	 */
    void onChangeColor(final int changeColorType);
}
//Gionee <zhaoyulong> <2015-05-13> add for CR01480269 end