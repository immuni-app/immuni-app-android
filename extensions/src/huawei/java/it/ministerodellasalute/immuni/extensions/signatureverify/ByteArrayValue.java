/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.ministerodellasalute.immuni.extensions.signatureverify;

import androidx.annotation.Nullable;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;

//import com.google.auto.value.AutoValue;

/**
 * Wrapper class providing value-semantics for {@code byte[]} fields, for example in @{@link
 * AutoValue} classes.
 *
 * <p>The behaviours which give value semantics are:
 *
 * <ul>
 *   <li>The constructor makes a copy of the input(s), to prevent mutation by the caller.
 *   <li>The getter returns a copy of the array, to prevent mutation by the receiver.
 *   <li>{@link #equals} is overridden to compare array content
 *   <li>{@link #hashCode} is overridden to return the hashCode of the array content.
 * </ul>
 * <p>
 * In some cases, the {@code clone()} is unnecessary overhead. Where it really matters, there is a
 * constructor which can take ownership of the {@code byte[]}.
 */
public class ByteArrayValue {
    private final byte[] bytes;

    /**
     * The length of the array.
     */
    public final int length;

    public ByteArrayValue(byte[] bytes) {
        this(bytes, /*takeOwnership=*/ false);
    }

    /**
     * Constructor used for cases where the ByteArrayValue takes ownership of the {@code byte[]},
     * avoiding the need to clone.
     */
    public ByteArrayValue(byte[] bytes, boolean takeOwnership) {
        this.bytes = takeOwnership ? bytes : bytes.clone();
        this.length = this.bytes.length;
    }

    public byte[] get() {
        return bytes.clone();
    }

    /**
     * Direct access to the internal {@code byte[]}. Use in derived classes for read-only access, to
     * avoid the overhead of cloning the array.
     *
     * <p>WARNING: This breaks the encapsulation and could allow the value to be mutated. Only use in
     * performance sensitive cases where the cost of cloning is too high, and the use of the {@code
     * byte[]} is guaranteed to be read-only.
     */
    protected byte[] exposeInternalBytesAndRiskMutation() {
        return bytes;
    }

    /**
     * Compares the content of the array.
     */
    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ByteArrayValue)) {
            return false;
        }
        ByteArrayValue that = (ByteArrayValue) other;
        return Arrays.equals(bytes, that.bytes);
    }

    /**
     * Returns a hashCode for the content of the array.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public String toString() {
        return new BigInteger(1, bytes).toString(16);
    }

    /**
     * Construct a {@link ByteArrayValue} from a possibly null array.
     *
     * <p>If {@code bytes} is null, return null.
     */
    @Nullable
    public static ByteArrayValue from(@Nullable byte[] bytes) {
        return (bytes != null) ? new ByteArrayValue(bytes) : null;
    }

    /**
     * Construct a value from the concatenation of 1 or more {@code byte[]} inputs.
     */
    public static ByteArrayValue concat(byte[]... arrays) {
        byte[] bytes = concatBytes(arrays);
        return new ByteArrayValue(bytes, /*takeOwnership=*/ true);
    }

    /**
     * Puts the data from this {@link ByteArrayValue} into a {@link ByteBuffer}.
     *
     * @param byteBuffer the destination buffer to {@link ByteBuffer#put} the byte representation.
     */
    public void putIn(ByteBuffer byteBuffer) {
        checkArgument(byteBuffer.remaining() >= length);
        byteBuffer.put(bytes);
    }

    /**
     * Returns the values from each provided array combined into a single array. For example, {@code
     * concat(new byte[] {a, b}, new byte[] {}, new byte[] {c}} returns the array {@code {a, b, c}}.
     *
     * @param arrays zero or more {@code byte} arrays
     * @return a single array containing all the values from the source arrays, in order
     */
    private static byte[] concatBytes(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }
        byte[] result = new byte[length];
        int pos = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, pos, array.length);
            pos += array.length;
        }
        return result;
    }
}