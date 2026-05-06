package com.godot.godot;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

/**
 * Wraps a native Godot Dictionary variant (type 27).
 * Provides key-value operations and enumeration support.
 * Implements AutoCloseable for native memory lifecycle management.
 */
public class GodotDictionary implements AutoCloseable {

    private static final long VARIANT_SIZE = 24; // 64-bit system

    private MemorySegment ptr; // The variant pointer
    private boolean ownsMemory;

    // ========== Cached Method Pointers ==========
    private static MethodHandle sizeMethod;
    private static MethodHandle isEmptyMethod;
    private static MethodHandle clearMethod;
    private static MethodHandle hasMethod;
    private static MethodHandle getMethod;
    private static MethodHandle setMethod;
    private static MethodHandle eraseMethod;
    private static MethodHandle keysMethod;
    private static MethodHandle valuesMethod;
    private static MethodHandle findKeyMethod;

    // ========== Constructors ==========

    /** Creates a new empty GodotDictionary with an owned native variant. */
    public static GodotDictionary createEmpty() {
        GodotDictionary dict = new GodotDictionary();
        dict.ptr = Arena.ofConfined().allocate(VARIANT_SIZE);
        dict.ownsMemory = true;
        // Initialize as nil variant first
        invoke(GodotBridge.variantNewNil, dict.ptr);
        // Then construct as Dictionary using variant_construct
        try {
            Arena arena = Arena.ofConfined();
            MemorySegment args = arena.allocate(ValueLayout.ADDRESS, 0);
            MemorySegment callError = arena.allocate(12);
            GodotBridge.variantConstruct.invoke(
                BuiltinMethodHashes.DICTIONARY_TYPE, dict.ptr, args, 0, callError);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to create empty Dictionary", t);
        }
        return dict;
    }

    /** Creates an empty GodotDictionary (backward-compatible constructor). */
    public GodotDictionary() {
        // Use createEmpty pattern but don't return - just initialize
        this.ptr = Arena.ofConfined().allocate(VARIANT_SIZE);
        this.ownsMemory = true;
        try {
            invoke(GodotBridge.variantNewNil, this.ptr);
            Arena arena = Arena.ofConfined();
            MemorySegment args = arena.allocate(ValueLayout.ADDRESS, 0);
            MemorySegment callError = arena.allocate(12);
            GodotBridge.variantConstruct.invoke(
                BuiltinMethodHashes.DICTIONARY_TYPE, this.ptr, args, 0, callError);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to create empty Dictionary", t);
        }
    }

    /** Wraps an existing variant pointer (copies the data). */
    public GodotDictionary(MemorySegment variantPtr) {
        // Verify it's a Dictionary type
        try {
            int type = (int) GodotBridge.variantGetType.invoke(variantPtr);
            if (type != BuiltinMethodHashes.DICTIONARY_TYPE) {
                throw new IllegalArgumentException("Variant is not type DICTIONARY, got: " + type);
            }
        } catch (Throwable t) {
            throw new RuntimeException("Failed to verify variant type", t);
        }

        this.ptr = Arena.ofConfined().allocate(VARIANT_SIZE);
        this.ownsMemory = true;
        try {
            // Copy the variant data
            invoke(GodotBridge.variantNewCopy, this.ptr, variantPtr);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to copy Dictionary variant", t);
        }
    }

    /** Creates from a GodotVariant (extracts and copies if it's a Dictionary). */
    public GodotDictionary(GodotVariant variant) {
        this(variant.ptr());
    }

    // ========== Query Operations ==========

    /** Returns the number of key-value pairs in the dictionary. */
    public int size() {
        checkValid();
        try {
            MethodHandle mh = getSizeMethod();
            if (mh == null) return 0;
            return (int) mh.invokeExact(ptr);
        } catch (Throwable t) {
            throw new RuntimeException("Dictionary.size() failed", t);
        }
    }

    /** Returns true if the dictionary is empty. */
    public boolean isEmpty() {
        checkValid();
        try {
            MethodHandle mh = getIsEmptyMethod();
            if (mh == null) return true;
            return (boolean) mh.invokeExact(ptr);
        } catch (Throwable t) {
            throw new RuntimeException("Dictionary.isEmpty() failed", t);
        }
    }

    /** Returns true if the dictionary contains the specified key. */
    public boolean has(Object key) {
        checkValid();
        try {
            MethodHandle mh = getHasMethod();
            if (mh == null) return false;

            try (GodotVariant keyVariant = GodotObject.toVariant(key)) {
                return (boolean) mh.invokeExact(ptr, keyVariant.ptr());
            }
        } catch (Throwable t) {
            return false;
        }
    }

    /** Returns the value associated with the key, or null if not found. */
    public Object get(Object key) {
        checkValid();
        try {
            MethodHandle mh = getGetMethod();
            if (mh == null) return null;

            Arena arena = Arena.ofConfined();
            // Convert key to variant
            try (GodotVariant keyVariant = GodotObject.toVariant(key);
                 GodotVariant defaultVariant = new GodotVariant()) { // nil default

                MemorySegment retVariant = arena.allocate(VARIANT_SIZE);
                // Initialize as nil
                invoke(GodotBridge.variantNewNil, retVariant);

                // Call get with key and default (nil)
                mh.invokeExact(ptr, keyVariant.ptr(), defaultVariant.ptr(), retVariant);

                return GodotObject.toJavaObjectStatic(retVariant);
            }
        } catch (Throwable t) {
            return null;
        }
    }

    // ========== Mutation Operations ==========

    /** Sets a key-value pair in the dictionary. */
    public void set(Object key, Object value) {
        checkValid();
        try {
            MethodHandle mh = getSetMethod();
            if (mh == null) return;

            // Dictionary uses variant_set_named for setting by string keys
            // But for arbitrary variant keys, we use variant_set
            // Actually, Dictionary.set is done via variant_set_named if key is String
            // or we need to use variant_set
            // Let's use the variant_set API with the key variant

            // For simplicity, use variantSetNamed with StringName conversion
            // If key is a String, this works directly
            try (Arena arena = Arena.ofConfined()) {
                if (key instanceof String strKey) {
                    // Use variant_set_named
                    try (GodotStringName keySN = new GodotStringName(strKey);
                         GodotVariant valueVariant = GodotObject.toVariant(value)) {
                        MemorySegment valid = arena.allocate(1);
                        // Create self as object variant
                        MemorySegment selfVariant = createSelfVariant(arena);
                        GodotBridge.variantSetNamed.invoke(selfVariant, keySN.ptr(), valueVariant.ptr(), valid);
                    }
                } else {
                    // For non-string keys, we need to use the variant_set with integer key
                    // Dictionary uses integer hash as key internally
                    // This is more complex - for now, only support String keys via setNamed
                    throw new IllegalArgumentException("Dictionary.set() currently only supports String keys");
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException("Dictionary.set() failed", t);
        }
    }

    /** Puts a key-value pair in the dictionary (alias for set). */
    public void put(Object key, Object value) {
        set(key, value);
    }

    /** Removes the key-value pair with the specified key. Returns true if the key existed. */
    public boolean remove(Object key) {
        checkValid();
        try {
            MethodHandle mh = getEraseMethod();
            if (mh == null) return false;

            try (GodotVariant keyVariant = GodotObject.toVariant(key)) {
                return (boolean) mh.invokeExact(ptr, keyVariant.ptr());
            }
        } catch (Throwable t) {
            return false;
        }
    }

    /** Removes all key-value pairs from the dictionary. */
    public void clear() {
        checkValid();
        try {
            MethodHandle mh = getClearMethod();
            if (mh != null) {
                mh.invokeExact(ptr);
            }
        } catch (Throwable t) {
            throw new RuntimeException("Dictionary.clear() failed", t);
        }
    }

    // ========== Enumeration Operations ==========

    /** Returns all keys as a GodotArray. */
    public GodotArray keys() {
        checkValid();
        try {
            MethodHandle mh = getKeysMethod();
            if (mh == null) return GodotArray.createEmpty();

            Arena arena = Arena.ofConfined();
            MemorySegment retVariant = arena.allocate(VARIANT_SIZE);
            // Initialize as nil
            invoke(GodotBridge.variantNewNil, retVariant);

            mh.invokeExact(ptr, retVariant);

            // The result should be an Array variant
            return new GodotArray(retVariant);
        } catch (Throwable t) {
            throw new RuntimeException("Dictionary.keys() failed", t);
        }
    }

    /** Returns all values as a GodotArray. */
    public GodotArray values() {
        checkValid();
        try {
            MethodHandle mh = getValuesMethod();
            if (mh == null) return GodotArray.createEmpty();

            Arena arena = Arena.ofConfined();
            MemorySegment retVariant = arena.allocate(VARIANT_SIZE);
            // Initialize as nil
            invoke(GodotBridge.variantNewNil, retVariant);

            mh.invokeExact(ptr, retVariant);

            // The result should be an Array variant
            return new GodotArray(retVariant);
        } catch (Throwable t) {
            throw new RuntimeException("Dictionary.values() failed", t);
        }
    }

    // ========== Variant Conversion ==========

    /** Converts this dictionary to a GodotVariant. */
    public GodotVariant toVariant() {
        if (ptr == null || ptr.equals(MemorySegment.NULL)) {
            return new GodotVariant();
        }
        // Return a new GodotVariant wrapping our owned variant
        return new GodotVariant(ptr);
    }

    /** Returns the raw variant pointer. */
    public MemorySegment ptr() {
        return ptr;
    }

    // ========== AutoCloseable ==========

    @Override
    public void close() {
        if (ownsMemory && ptr != null && !ptr.equals(MemorySegment.NULL)) {
            invoke(GodotBridge.variantDestroy, ptr);
            ptr = null;
        }
    }

    // ========== Helper Methods ==========

    private void checkValid() {
        if (ptr == null || ptr.equals(MemorySegment.NULL)) {
            throw new IllegalStateException("GodotDictionary is closed");
        }
    }

    private static void invoke(MethodHandle mh, Object... args) {
        try {
            switch (args.length) {
                case 1 -> mh.invoke(args[0]);
                case 2 -> mh.invoke(args[0], args[1]);
                case 3 -> mh.invoke(args[0], args[1], args[2]);
                default -> mh.invokeWithArguments(args);
            }
        } catch (Throwable t) {
            throw new RuntimeException("Panama call failed", t);
        }
    }

    /** Creates a self-variant for named access (used with variantSetNamed). */
    private MemorySegment createSelfVariant(Arena arena) {
        try {
            int DICTIONARY_TYPE = 27;
            MethodHandle ctor = GodotVariant.getTypeConstructor(DICTIONARY_TYPE);
            if (ctor == null) return MemorySegment.NULL;

            MemorySegment variant = arena.allocate(VARIANT_SIZE);
            // Pass our dictionary's variant pointer as the "value" to construct from
            ctor.invoke(variant, ptr);
            return variant;
        } catch (Throwable t) {
            return MemorySegment.NULL;
        }
    }

    // ========== Builtin Method Resolution ==========

    private static MethodHandle getSizeMethod() {
        if (sizeMethod != null) return sizeMethod;
        sizeMethod = resolveBuiltinMethod("size", BuiltinMethodHashes.DICTIONARY_SIZE);
        return sizeMethod;
    }

    private static MethodHandle getIsEmptyMethod() {
        if (isEmptyMethod != null) return isEmptyMethod;
        isEmptyMethod = resolveBuiltinMethod("is_empty", BuiltinMethodHashes.DICTIONARY_IS_EMPTY);
        return isEmptyMethod;
    }

    private static MethodHandle getClearMethod() {
        if (clearMethod != null) return clearMethod;
        clearMethod = resolveBuiltinMethod("clear", BuiltinMethodHashes.DICTIONARY_CLEAR);
        return clearMethod;
    }

    private static MethodHandle getHasMethod() {
        if (hasMethod != null) return hasMethod;
        hasMethod = resolveBuiltinMethod("has", BuiltinMethodHashes.DICTIONARY_HAS);
        return hasMethod;
    }

    private static MethodHandle getGetMethod() {
        if (getMethod != null) return getMethod;
        getMethod = resolveBuiltinMethod("get", BuiltinMethodHashes.DICTIONARY_GET);
        return getMethod;
    }

    private static MethodHandle getSetMethod() {
        if (setMethod != null) return setMethod;
        // Dictionary doesn't have a set method via variant_get_ptr_builtin_method
        // We use variant_set_named instead
        return null;
    }

    private static MethodHandle getEraseMethod() {
        if (eraseMethod != null) return eraseMethod;
        eraseMethod = resolveBuiltinMethod("erase", BuiltinMethodHashes.DICTIONARY_ERASE);
        return eraseMethod;
    }

    private static MethodHandle getKeysMethod() {
        if (keysMethod != null) return keysMethod;
        keysMethod = resolveBuiltinMethod("keys", BuiltinMethodHashes.DICTIONARY_KEYS);
        return keysMethod;
    }

    private static MethodHandle getValuesMethod() {
        if (valuesMethod != null) return valuesMethod;
        valuesMethod = resolveBuiltinMethod("values", BuiltinMethodHashes.DICTIONARY_VALUES);
        return valuesMethod;
    }

    private static MethodHandle getFindKeyMethod() {
        if (findKeyMethod != null) return findKeyMethod;
        findKeyMethod = resolveBuiltinMethod("find_key", BuiltinMethodHashes.DICTIONARY_FIND_KEY);
        return findKeyMethod;
    }

    /**
     * Resolves a builtin method via variant_get_ptr_builtin_method.
     * For Dictionary methods, the signatures vary:
     * - size/is_empty/clear: void (const Dictionary* self)
     * - has/erase: void (const Dictionary* self, const Variant* key)
     * - get: void (const Dictionary* self, const Variant* key, const Variant* default, Variant* r_ret)
     * - keys/values: void (const Dictionary* self, Array* r_ret)
     */
    private static MethodHandle resolveBuiltinMethod(String methodName, long hash) {
        try (Arena arena = Arena.ofConfined();
             GodotStringName sn = new GodotStringName(methodName)) {

            MemorySegment funcPtr = (MemorySegment) GodotBridge.variantGetPtrBuiltinMethod.invoke(
                BuiltinMethodHashes.DICTIONARY_TYPE, sn.ptr(), hash);

            if (funcPtr == null || funcPtr.equals(MemorySegment.NULL)) {
                System.err.println("godot-java: Failed to resolve builtin method: Dictionary." + methodName);
                return null;
            }

            // For Dictionary, the first parameter is a pointer to the dictionary data
            // which is different from the variant pointer itself
            // We'll use a generic 3-argument signature and adjust as needed
            MethodHandle mh = Linker.nativeLinker().downcallHandle(funcPtr,
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));

            return mh;
        } catch (Throwable t) {
            System.err.println("godot-java: Error resolving builtin method: Dictionary." + methodName + ": " + t.getMessage());
            return null;
        }
    }
}