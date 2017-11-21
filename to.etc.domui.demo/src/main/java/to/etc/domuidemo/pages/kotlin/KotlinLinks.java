package to.etc.domuidemo.pages.kotlin;

import to.etc.domuidemo.pages.MenuPage;
import to.etc.domuidemo.pages.kotlin.basic.KtPage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-11-17.
 */
public class KotlinLinks extends MenuPage {
	public KotlinLinks() {
		super("Kotlin code");
	}

	@Override public void createContent() throws Exception {
		addLink(KtPage.class, "Hello world, Kotlin style");
	}
}
