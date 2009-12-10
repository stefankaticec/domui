package to.etc.domui.caches.images;

import java.io.*;

import javax.annotation.concurrent.*;

import to.etc.domui.caches.filecache.*;

/**
 * The base for a cached image thingerydoo. This is maintained by
 * the ImageCache.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 9, 2009
 */
class CachedImageFragment {
	/** The root descriptor of the image's base */
	private final ImageRoot m_imageRoot;

	/** An unique string describing the permutation of the original that this contains. When "" (empty string) this is the ORIGINAL image. */
	private final String m_permutation;

	/** The versionLong of the source for this image at the time it was created. */
	@GuardedBy("getRoot()")
	private long m_sourceVersionLong;

	/**
	 * The LRU pointers for the cache's LRU list. These are locked and maintained by the ImageCache itself; access to these is "verboten" from self.
	 */
	@GuardedBy("cache()")
	CachedImageFragment m_lruPrev, m_lruNext;

	@GuardedBy("cache()")
	InstanceCacheState m_cacheState;

	/** The current actual memory size taken by this entry. */
	@GuardedBy("getRoot()")
	private long m_memoryCacheSize;

	/** The cacheref for the file while this thingy is in use. */
	private FileCacheRef m_fileRef;

	CachedImageFragment(final ImageRoot root, final String perm, long sourceVersionLong, int memorysize, FileCacheRef ref) {
		m_imageRoot = root;
		m_permutation = perm;
		m_sourceVersionLong = sourceVersionLong;
		m_memoryCacheSize = memorysize;
		m_fileRef = ref;
	}

	/**
	 * Return the image root cache entry.
	 * @return
	 */
	final ImageRoot getRoot() {
		return m_imageRoot;
	}

	final public String getPermutation() {
		return m_permutation;
	}

	final public boolean isOriginal() {
		return m_permutation.length() == 0;
	}

	final public ImageCache cache() {
		return getRoot().getCache();
	}

	public long getMemoryCacheSize() {
		return m_memoryCacheSize;
	}

	final public File getFile() {
		return m_fileRef.getFile();
	}

	final public FileCacheRef getFileRef() {
		return m_fileRef;
	}
}
