package to.etc.domui.databinding;

import javax.annotation.*;

public interface IValueChangeListener<T> extends IChangeListener<T, ValueChangeEvent<T>, IValueChangeListener<T>> {
	@Override
	void handleChange(@Nonnull ValueChangeEvent<T> event) throws Exception;
}
