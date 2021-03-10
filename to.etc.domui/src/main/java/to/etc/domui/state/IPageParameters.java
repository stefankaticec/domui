package to.etc.domui.state;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public interface IPageParameters extends IBasicParameterContainer {
	/**
	 * Creates copy of current PageParameters.
	 * Since modification of live page params is not allowed, in order to navigate to other page with similar set of params, use this method to get params template for new page navigation.
	 */
	@NonNull
	PageParameters getUnlockedCopy();

	/**
	 * Indicates whether a given parameter name exists in this PageParameters object.
	 *
	 * @param name, the name of the parameter to be checked for.
	 * @return true when the parameter exists, false otherwise.
	 */
	boolean hasParameter(@NonNull String name);

	/**
	 * Gets the value for the specified parametername as an int (primitive).
	 * If the parameter does not exists or the value cannot be converted to an int, a MissingParameterException is thrown.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @return the value as an int
	 */
	int getInt(@NonNull String name);

	/**
	 * Gets the value for the specified parametername as an int (primitive).
	 * If the parameter does cannot be converted to an int, a MissingParameterException is thrown.
	 * When the parameter does not exist, the specified default value is returned.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @param df, the default value to be returned, when the specified parameter does not exist.
	 * @return the value as an int
	 */
	int getInt(@NonNull String name, int df);

	/**
	 * Gets the value for the specified parametername as a long (primitive).
	 * When multiple value exists for the specified parameter, the first element of the array is returned.
	 * If the parameter does not exists or the value cannot be converted to an long, a MissingParameterException is thrown.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @return the value as a long
	 */
	long getLong(@NonNull String name);

	/**
	 * Gets the value for the specified parametername as a long (primitive).
	 * When multiple value exists for the specified parameter, the first element of the array is returned.
	 * If the parameter does cannot be converted to an long, a MissingParameterException is thrown.
	 * When the parameter does not exist, the specified default value is returned.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @param df, the default value to be returned, when the specified parameter does not exist.
	 * @return the value as a long
	 */
	long getLong(@NonNull String name, long df);

	/**
	 * Gets the value for the specified parametername as a boolean (primitive).
	 * When multiple value exists for the specified parameter, the first element of the array is returned.
	 * If the parameter does not exists or the value cannot be converted to an boolean, a MissingParameterException is thrown.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @return the value as a long
	 */
	boolean getBoolean(@NonNull String name);

	/**
	 * Gets the value for the specified parametername as a boolean (primitive).
	 * When multiple value exists for the specified parameter, the first element of the array is returned.
	 * If the parameter does cannot be converted to an boolean, a MissingParameterException is thrown.
	 * When the parameter does not exist, the specified default value is returned.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @param df, the default value to be returned, when the specified parameter does not exist.
	 * @return the value as a boolean
	 */
	boolean getBoolean(@NonNull String name, boolean df);

	/**
	 * Gets the value for the specified parametername as a Long object.
	 * When multiple value exists for the specified parameter, the first element of the array is returned.
	 * If the parameter does not exists or the value cannot be converted to an int, a MissingParameterException is thrown.
	 * This method uses decode() so hexadecimal and octal strings can be used as parameter values.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @return the value as a Long
	 */
	Long getLongW(@NonNull String name);

	/**
	 * Gets the value for the specified parametername as a Long object.
	 * When multiple value exists for the specified parameter, the first element of the array is returned.
	 * If the parameter does cannot be converted to an int, a MissingParameterException is thrown.
	 * When the parameter does not exist, the specified default value is returned.
	 * This method uses decode() so hexadecimal and octal strings can be used as parameter values.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @param df, the default value to be returned, when the specified parameter does not exist.
	 * @return the value as a Long
	 */
	Long getLongW(@NonNull String name, long df);

	/**
	 * Gets the value for the specified parametername as a Long object.
	 * When multiple value exists for the specified parameter, the first element of the array is returned.
	 * If the parameter does cannot be converted to an int, a MissingParameterException is thrown.
	 * When the parameter does not exist, the specified default value is returned.
	 * This method uses decode() so hexadecimal and octal strings can be used as parameter values.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @param df, the default value to be returned, when the specified parameter does not exist.
	 * @return the value as a Long
	 */
	Long getLongW(@NonNull String name, @Nullable Long df);

	/**
	 * Gets the value for the specified parametername as a String object.
	 * When multiple value exists for the specified parameter, a MultipleParameterException is thrown.
	 * When the parameter does not exist, a MissingParameterException is thrown.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @return the value as a String
	 */
	@NonNull String getString(@NonNull String name);

	/**
	 * Gets the value for the specified parametername as a String object.
	 * When multiple value exists for the specified parameter, a MultipleParameterException is thrown.
	 * When the parameter does not exist, the specified default value is returned.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @param df, the default value to be returned, when the specified parameter does not exist.
	 * @return the value as a String
	 */
	@Nullable String getString(@NonNull String name, @Nullable String df);

	/**
	 * Gets the value for the specified parametername as a String array.
	 * When the parameter does not exist, a MissingParameterException is thrown.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @return the value as a String
	 */
	@NonNull String[] getStringArray(@NonNull String name);

	/**
	 * DANGEROUS: Get the raw, unchecked parameter values directly from the request. These
	 * are UNCHECKED for XSS attacks, so anyone calling this NEEDS TO DO THAT BY THEMSELVES!!
	 * You would not normally call this method <b>at all</b>, but use {@link #getStringArray(String)} of
	 * course. The only reason to call this is for components that actually <b>do</b> expect XSS sensitive
	 * data in their values, like CKEditor - which expects &lt;img src=....&gt; tags in its input. These
	 * components ARE REQUIRED to still check for xss attacks, but now by themselves!!
	 */
	@Nullable String[] getRawUnsafeStringArray(@NonNull String name);

	@Nullable String[] getStringArray(@NonNull String name, @Nullable String[] deflt);

	/**
	 * Compare this with another instance. Used to see that a new request has different parameters
	 * than an earlier request.
	 * <h2>remark</h2>
	 * <p>We check the size of the maps; if they are equal we ONLY have to check that each key-value
	 * pair in SOURCE exists in TARGET AND is the same. We don't need to check for "thingies in SRC
	 * that do not occur in TGT" because that cannot happen if the map sizes are equal.</p>
	 */
	@Override boolean equals(Object obj);

	@Override int hashCode();

	/**
	 * EXPENSIVE Hash all parameter values into an MD5 hash. This must be repeatable so same parameters get the same hash code.
	 */
	@NonNull String calculateHashString();

	boolean isReadOnly();
}
