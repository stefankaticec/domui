package to.etc.qmodel.test;

import org.junit.Test;
import to.etc.qmodel.ProxyGenerator;

import java.util.Arrays;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 17-2-19.
 */
public class TestQModelProxies {
	@Test
	public void testSimpleProxy() throws Exception {
		PojoClass pc = new PojoClass("Frits", 10, Arrays.asList("Hello", "World"));
		PojoClass proxy = new ProxyGenerator().createPojoProxy(null, pc);

		String name = proxy.getName();
		System.out.println(">> name = " + name);
	}


}
