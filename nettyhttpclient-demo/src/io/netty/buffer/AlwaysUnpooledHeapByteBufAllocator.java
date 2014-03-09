package io.netty.buffer;

public class AlwaysUnpooledHeapByteBufAllocator extends AbstractByteBufAllocator {

	@Override
	public boolean isDirectBufferPooled() {
		return false;
	}

	@Override
	protected ByteBuf newHeapBuffer(int initialCapacity, int maxCapacity) {
        return new UnpooledHeapByteBuf(this, initialCapacity, maxCapacity);
	}

	@Override
	protected ByteBuf newDirectBuffer(int initialCapacity,
			int maxCapacity) {
		return newHeapBuffer(initialCapacity, maxCapacity);
	}
};
