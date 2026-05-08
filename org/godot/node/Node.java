package org.godot.node;

import java.lang.foreign.MemorySegment;
import java.util.Map;
import org.godot.Godot;
import org.godot.bridge.Bridge;
import org.godot.collection.GodotArray;
import org.godot.core.GodotStringName;
import org.godot.internal.api.ApiIndex;

public class Node extends Object {
	private static final String GODOT_CLASS_NAME = "Node";

	private static final Map<String, Long> METHOD_HASHES = java.util.Map.ofEntries(java.util.Map.entry("propagate_notification", 1286410249L), java.util.Map.entry("get_process_delta_time", 1740695150L), java.util.Map.entry("remove_child", 1078189570L), java.util.Map.entry("add_to_group", 3683006648L), java.util.Map.entry("set_translation_domain_inherited", 3218959716L), java.util.Map.entry("request_ready", 3218959716L), java.util.Map.entry("notify_thread_safe", 1286410249L), java.util.Map.entry("set_name", 3304788590L), java.util.Map.entry("set_process_thread_messages", 1357280998L), java.util.Map.entry("get_multiplayer_authority", 3905245786L), java.util.Map.entry("call_deferred_thread_group", 3400424181L), java.util.Map.entry("get_physics_process_priority", 3905245786L), java.util.Map.entry("get_last_exclusive_window", 1757182445L), java.util.Map.entry("is_editable_instance", 3093956946L), java.util.Map.entry("get_name", 2002593661L), java.util.Map.entry("get_parent", 3160264692L), java.util.Map.entry("set_multiplayer_authority", 972357352L), java.util.Map.entry("is_physics_interpolated", 36873697L), java.util.Map.entry("set_unique_name_in_owner", 2586408642L), java.util.Map.entry("call_thread_safe", 3400424181L), java.util.Map.entry("is_in_group", 2619796661L), java.util.Map.entry("is_processing_unhandled_input", 36873697L), java.util.Map.entry("set_thread_safe", 3776071444L), java.util.Map.entry("set_process_mode", 1841290486L), java.util.Map.entry("get_tree_string_pretty", 2841200299L), java.util.Map.entry("get_tree", 2958820483L), java.util.Map.entry("notify_deferred_thread_group", 1286410249L), java.util.Map.entry("get_editor_description", 201670096L), java.util.Map.entry("is_physics_processing", 36873697L), java.util.Map.entry("is_processing_input", 36873697L), java.util.Map.entry("is_processing_shortcut_input", 36873697L), java.util.Map.entry("replace_by", 2570952461L), java.util.Map.entry("atr_n", 259354841L), java.util.Map.entry("find_parent", 1140089439L), java.util.Map.entry("set_process", 2586408642L), java.util.Map.entry("get_physics_process_delta_time", 1740695150L), java.util.Map.entry("queue_accessibility_update", 3218959716L), java.util.Map.entry("update_configuration_warnings", 3218959716L), java.util.Map.entry("set_physics_process_priority", 1286410249L), java.util.Map.entry("set_process_thread_group_order", 1286410249L), java.util.Map.entry("rpc_id", 361499283L), java.util.Map.entry("is_node_ready", 36873697L), java.util.Map.entry("reparent", 3685795103L), java.util.Map.entry("queue_free", 3218959716L), java.util.Map.entry("get_index", 894402480L), java.util.Map.entry("set_process_thread_group", 2275442745L), java.util.Map.entry("get_node_rpc_config", 1214101251L), java.util.Map.entry("get_multiplayer", 406750475L), java.util.Map.entry("set_process_internal", 2586408642L), java.util.Map.entry("add_child", 3863233950L), java.util.Map.entry("create_tween", 3426978995L), java.util.Map.entry("get_physics_interpolation_mode", 2920385216L), java.util.Map.entry("set_deferred_thread_group", 3776071444L), java.util.Map.entry("can_auto_translate", 36873697L), java.util.Map.entry("get_process_thread_messages", 4228993612L), java.util.Map.entry("move_child", 3315886247L), java.util.Map.entry("get_child", 541253412L), java.util.Map.entry("get_children", 873284517L), java.util.Map.entry("is_inside_tree", 36873697L), java.util.Map.entry("get_node", 2734337346L), java.util.Map.entry("is_processing_internal", 36873697L), java.util.Map.entry("get_node_or_null", 2734337346L), java.util.Map.entry("is_displayed_folded", 36873697L), java.util.Map.entry("set_editable_instance", 2731852923L), java.util.Map.entry("is_processing_unhandled_key_input", 36873697L), java.util.Map.entry("set_display_folded", 2586408642L), java.util.Map.entry("get_path_to", 498846349L), java.util.Map.entry("propagate_call", 1871007965L), java.util.Map.entry("set_physics_process", 2586408642L), java.util.Map.entry("is_multiplayer_authority", 36873697L), java.util.Map.entry("get_accessibility_element", 2944877500L), java.util.Map.entry("find_children", 2560337219L), java.util.Map.entry("print_orphan_nodes", 3218959716L), java.util.Map.entry("get_process_mode", 739966102L), java.util.Map.entry("set_physics_interpolation_mode", 3202404928L), java.util.Map.entry("set_scene_instance_load_placeholder", 2586408642L), java.util.Map.entry("set_process_priority", 1286410249L), java.util.Map.entry("is_unique_name_in_owner", 36873697L), java.util.Map.entry("atr", 3344478075L), java.util.Map.entry("get_groups", 3995934104L), java.util.Map.entry("get_path", 4075236667L), java.util.Map.entry("has_node_and_resource", 861721659L), java.util.Map.entry("is_part_of_edited_scene", 36873697L), java.util.Map.entry("get_owner", 3160264692L), java.util.Map.entry("print_tree_pretty", 3218959716L), java.util.Map.entry("get_process_priority", 3905245786L), java.util.Map.entry("set_process_unhandled_input", 2586408642L), java.util.Map.entry("set_scene_file_path", 83702148L), java.util.Map.entry("duplicate", 3511555459L), java.util.Map.entry("get_scene_instance_load_placeholder", 36873697L), java.util.Map.entry("get_auto_translate_mode", 2498906432L), java.util.Map.entry("set_physics_process_internal", 2586408642L), java.util.Map.entry("rpc_config", 3776071444L), java.util.Map.entry("print_tree", 3218959716L), java.util.Map.entry("set_process_unhandled_key_input", 2586408642L), java.util.Map.entry("get_scene_file_path", 201670096L), java.util.Map.entry("remove_from_group", 3304788590L), java.util.Map.entry("set_owner", 1078189570L), java.util.Map.entry("get_orphan_node_ids", 2915620761L), java.util.Map.entry("get_process_thread_group", 1866404740L), java.util.Map.entry("set_process_shortcut_input", 2586408642L), java.util.Map.entry("get_node_and_resource", 502563882L), java.util.Map.entry("can_process", 36873697L), java.util.Map.entry("is_physics_processing_internal", 36873697L), java.util.Map.entry("find_child", 2008217037L), java.util.Map.entry("is_ancestor_of", 3093956946L), java.util.Map.entry("set_auto_translate_mode", 776149714L), java.util.Map.entry("is_processing", 36873697L), java.util.Map.entry("get_viewport", 3596683776L), java.util.Map.entry("get_child_count", 894402480L), java.util.Map.entry("is_physics_interpolated_and_enabled", 36873697L), java.util.Map.entry("set_process_input", 2586408642L), java.util.Map.entry("rpc", 4047867050L), java.util.Map.entry("has_node", 861721659L), java.util.Map.entry("get_window", 1757182445L), java.util.Map.entry("get_tree_string", 2841200299L), java.util.Map.entry("get_process_thread_group_order", 3905245786L), java.util.Map.entry("set_editor_description", 83702148L), java.util.Map.entry("add_sibling", 2570952461L), java.util.Map.entry("is_greater_than", 3093956946L), java.util.Map.entry("reset_physics_interpolation", 3218959716L));

	public Node(MemorySegment nativePointer) {
		super(nativePointer);
	}

	public Node(long nativePointer) {
		super(nativePointer);
	}

	public Node() {
		super();
	}

	public static void printOrphanNodes() {
		callStatic("Node", "print_orphan_nodes", 3218959716L);
	}

	public static long[] getOrphanNodeIds() {
		return (long[]) callStatic("Node", "get_orphan_node_ids", 2915620761L);
	}

	public void addSibling(Node sibling, boolean force_readable_name) {
		super.call("add_sibling", new java.lang.Object[] { (java.lang.Object) sibling, java.lang.Boolean.valueOf(force_readable_name) });
	}

	public void addSibling(Node sibling) {
		addSibling(sibling, false);
	}

	public void addChild(Node node, boolean force_readable_name, int internal) {
		super.call("add_child", new java.lang.Object[] { (java.lang.Object) node, java.lang.Boolean.valueOf(force_readable_name), java.lang.Integer.valueOf(internal) });
	}

	public void addChild(Node node, boolean force_readable_name) {
		addChild(node, force_readable_name, 0);
	}

	public void addChild(Node node) {
		addChild(node, false, 0);
	}

	public void removeChild(Node node) {
		super.call("remove_child", new java.lang.Object[] { (java.lang.Object) node });
	}

	public void reparent(Node new_parent, boolean keep_global_transform) {
		super.call("reparent", new java.lang.Object[] { (java.lang.Object) new_parent, java.lang.Boolean.valueOf(keep_global_transform) });
	}

	public void reparent(Node new_parent) {
		reparent(new_parent, true);
	}

	public int getChildCount(boolean include_internal) {
		return (int) super.call("get_child_count", new java.lang.Object[] { java.lang.Boolean.valueOf(include_internal) });
	}

	public int getChildCount() {
		return getChildCount(false);
	}

	public Node[] getChildren(boolean include_internal) {
		return (Node[]) super.call("get_children", new java.lang.Object[] { java.lang.Boolean.valueOf(include_internal) });
	}

	public Node[] getChildren() {
		return getChildren(false);
	}

	public Node getChild(long idx, boolean include_internal) {
		return (Node) super.call("get_child", new java.lang.Object[] { java.lang.Long.valueOf(idx), java.lang.Boolean.valueOf(include_internal) });
	}

	public Node getChild(long idx) {
		return getChild(idx, false);
	}

	public boolean hasNode(String path) {
		return (boolean) super.call("has_node", new java.lang.Object[] { (java.lang.Object) path });
	}

	public Node getNode(String path) {
		return (Node) super.call("get_node", new java.lang.Object[] { (java.lang.Object) path });
	}

	public Node getNodeOrNull(String path) {
		return (Node) super.call("get_node_or_null", new java.lang.Object[] { (java.lang.Object) path });
	}

	public Node getParent() {
		return (Node) super.call("get_parent");
	}

	public Node findChild(String pattern, boolean recursive, boolean owned) {
		return (Node) super.call("find_child", new java.lang.Object[] { (java.lang.Object) pattern, java.lang.Boolean.valueOf(recursive), java.lang.Boolean.valueOf(owned) });
	}

	public Node findChild(String pattern, boolean recursive) {
		return findChild(pattern, recursive, true);
	}

	public Node findChild(String pattern) {
		return findChild(pattern, true, true);
	}

	public Node[] findChildren(String pattern, String type, boolean recursive, boolean owned) {
		return (Node[]) super.call("find_children", new java.lang.Object[] { (java.lang.Object) pattern, (java.lang.Object) type, java.lang.Boolean.valueOf(recursive), java.lang.Boolean.valueOf(owned) });
	}

	public Node[] findChildren(String pattern, String type, boolean recursive) {
		return findChildren(pattern, type, recursive, true);
	}

	public Node[] findChildren(String pattern, String type) {
		return findChildren(pattern, type, true, true);
	}

	public Node[] findChildren(String pattern) {
		return findChildren(pattern, "", true, true);
	}

	public Node findParent(String pattern) {
		return (Node) super.call("find_parent", new java.lang.Object[] { (java.lang.Object) pattern });
	}

	public boolean hasNodeAndResource(String path) {
		return (boolean) super.call("has_node_and_resource", new java.lang.Object[] { (java.lang.Object) path });
	}

	public GodotArray getNodeAndResource(String path) {
		return (GodotArray) super.call("get_node_and_resource", new java.lang.Object[] { (java.lang.Object) path });
	}

	public boolean isInsideTree() {
		return (boolean) super.call("is_inside_tree");
	}

	public boolean isPartOfEditedScene() {
		return (boolean) super.call("is_part_of_edited_scene");
	}

	public boolean isAncestorOf(Node node) {
		return (boolean) super.call("is_ancestor_of", new java.lang.Object[] { (java.lang.Object) node });
	}

	public boolean isGreaterThan(Node node) {
		return (boolean) super.call("is_greater_than", new java.lang.Object[] { (java.lang.Object) node });
	}

	public String getPath() {
		return (String) super.call("get_path");
	}

	public String getPathTo(Node node, boolean use_unique_path) {
		return (String) super.call("get_path_to", new java.lang.Object[] { (java.lang.Object) node, java.lang.Boolean.valueOf(use_unique_path) });
	}

	public String getPathTo(Node node) {
		return getPathTo(node, false);
	}

	public void addToGroup(String group, boolean persistent) {
		super.call("add_to_group", new java.lang.Object[] { (java.lang.Object) group, java.lang.Boolean.valueOf(persistent) });
	}

	public void addToGroup(String group) {
		addToGroup(group, false);
	}

	public void removeFromGroup(String group) {
		super.call("remove_from_group", new java.lang.Object[] { (java.lang.Object) group });
	}

	public boolean isInGroup(String group) {
		return (boolean) super.call("is_in_group", new java.lang.Object[] { (java.lang.Object) group });
	}

	public void moveChild(Node child_node, long to_index) {
		super.call("move_child", new java.lang.Object[] { (java.lang.Object) child_node, java.lang.Long.valueOf(to_index) });
	}

	public String[] getGroups() {
		return (String[]) super.call("get_groups");
	}

	public int getIndex(boolean include_internal) {
		return (int) super.call("get_index", new java.lang.Object[] { java.lang.Boolean.valueOf(include_internal) });
	}

	public int getIndex() {
		return getIndex(false);
	}

	public void printTree() {
		super.call("print_tree");
	}

	public void printTreePretty() {
		super.call("print_tree_pretty");
	}

	public String getTreeString() {
		return (String) super.call("get_tree_string");
	}

	public String getTreeStringPretty() {
		return (String) super.call("get_tree_string_pretty");
	}

	public void propagateNotification(long what) {
		super.call("propagate_notification", new java.lang.Object[] { java.lang.Long.valueOf(what) });
	}

	public void propagateCall(String method, GodotArray args, boolean parent_first) {
		super.call("propagate_call", new java.lang.Object[] { (java.lang.Object) method, (java.lang.Object) args, java.lang.Boolean.valueOf(parent_first) });
	}

	public void propagateCall(String method, GodotArray args) {
		propagateCall(method, args, false);
	}

	public void propagateCall(String method) {
		propagateCall(method, null, false);
	}

	public void setPhysicsProcess(boolean enable) {
		super.call("set_physics_process", new java.lang.Object[] { java.lang.Boolean.valueOf(enable) });
	}

	public double getPhysicsProcessDeltaTime() {
		return (double) super.call("get_physics_process_delta_time");
	}

	public boolean isPhysicsProcessing() {
		return (boolean) super.call("is_physics_processing");
	}

	public double getProcessDeltaTime() {
		return (double) super.call("get_process_delta_time");
	}

	public void setProcess(boolean enable) {
		super.call("set_process", new java.lang.Object[] { java.lang.Boolean.valueOf(enable) });
	}

	public void setPhysicsProcessPriority(long priority) {
		super.call("set_physics_process_priority", new java.lang.Object[] { java.lang.Long.valueOf(priority) });
	}

	public int getPhysicsProcessPriority() {
		return (int) super.call("get_physics_process_priority");
	}

	public boolean isProcessing() {
		return (boolean) super.call("is_processing");
	}

	public void setProcessInput(boolean enable) {
		super.call("set_process_input", new java.lang.Object[] { java.lang.Boolean.valueOf(enable) });
	}

	public boolean isProcessingInput() {
		return (boolean) super.call("is_processing_input");
	}

	public void setProcessShortcutInput(boolean enable) {
		super.call("set_process_shortcut_input", new java.lang.Object[] { java.lang.Boolean.valueOf(enable) });
	}

	public boolean isProcessingShortcutInput() {
		return (boolean) super.call("is_processing_shortcut_input");
	}

	public void setProcessUnhandledInput(boolean enable) {
		super.call("set_process_unhandled_input", new java.lang.Object[] { java.lang.Boolean.valueOf(enable) });
	}

	public boolean isProcessingUnhandledInput() {
		return (boolean) super.call("is_processing_unhandled_input");
	}

	public void setProcessUnhandledKeyInput(boolean enable) {
		super.call("set_process_unhandled_key_input", new java.lang.Object[] { java.lang.Boolean.valueOf(enable) });
	}

	public boolean isProcessingUnhandledKeyInput() {
		return (boolean) super.call("is_processing_unhandled_key_input");
	}

	public boolean canProcess() {
		return (boolean) super.call("can_process");
	}

	public void queueAccessibilityUpdate() {
		super.call("queue_accessibility_update");
	}

	public long getAccessibilityElement() {
		return (long) super.call("get_accessibility_element");
	}

	public void setDisplayFolded(boolean fold) {
		super.call("set_display_folded", new java.lang.Object[] { java.lang.Boolean.valueOf(fold) });
	}

	public boolean isDisplayedFolded() {
		return (boolean) super.call("is_displayed_folded");
	}

	public void setProcessInternal(boolean enable) {
		super.call("set_process_internal", new java.lang.Object[] { java.lang.Boolean.valueOf(enable) });
	}

	public boolean isProcessingInternal() {
		return (boolean) super.call("is_processing_internal");
	}

	public void setPhysicsProcessInternal(boolean enable) {
		super.call("set_physics_process_internal", new java.lang.Object[] { java.lang.Boolean.valueOf(enable) });
	}

	public boolean isPhysicsProcessingInternal() {
		return (boolean) super.call("is_physics_processing_internal");
	}

	public boolean isPhysicsInterpolated() {
		return (boolean) super.call("is_physics_interpolated");
	}

	public boolean isPhysicsInterpolatedAndEnabled() {
		return (boolean) super.call("is_physics_interpolated_and_enabled");
	}

	public void resetPhysicsInterpolation() {
		super.call("reset_physics_interpolation");
	}

	public boolean canAutoTranslate() {
		return (boolean) super.call("can_auto_translate");
	}

	public void setTranslationDomainInherited() {
		super.call("set_translation_domain_inherited");
	}

	public Window getWindow() {
		return (Window) super.call("get_window");
	}

	public Window getLastExclusiveWindow() {
		return (Window) super.call("get_last_exclusive_window");
	}

	public SceneTree getTree() {
		return (SceneTree) super.call("get_tree");
	}

	public Tween createTween() {
		return (Tween) super.call("create_tween");
	}

	public Node duplicate(long flags) {
		return (Node) super.call("duplicate", new java.lang.Object[] { java.lang.Long.valueOf(flags) });
	}

	public Node duplicate() {
		return duplicate(15L);
	}

	public void replaceBy(Node node, boolean keep_groups) {
		super.call("replace_by", new java.lang.Object[] { (java.lang.Object) node, java.lang.Boolean.valueOf(keep_groups) });
	}

	public void replaceBy(Node node) {
		replaceBy(node, false);
	}

	public void setSceneInstanceLoadPlaceholder(boolean load_placeholder) {
		super.call("set_scene_instance_load_placeholder", new java.lang.Object[] { java.lang.Boolean.valueOf(load_placeholder) });
	}

	public boolean getSceneInstanceLoadPlaceholder() {
		return (boolean) super.call("get_scene_instance_load_placeholder");
	}

	public void setEditableInstance(Node node, boolean is_editable) {
		super.call("set_editable_instance", new java.lang.Object[] { (java.lang.Object) node, java.lang.Boolean.valueOf(is_editable) });
	}

	public boolean isEditableInstance(Node node) {
		return (boolean) super.call("is_editable_instance", new java.lang.Object[] { (java.lang.Object) node });
	}

	public Viewport getViewport() {
		return (Viewport) super.call("get_viewport");
	}

	public void queueFree() {
		super.call("queue_free");
	}

	public void requestReady() {
		super.call("request_ready");
	}

	public boolean isNodeReady() {
		return (boolean) super.call("is_node_ready");
	}

	public void setMultiplayerAuthority(long id, boolean recursive) {
		super.call("set_multiplayer_authority", new java.lang.Object[] { java.lang.Long.valueOf(id), java.lang.Boolean.valueOf(recursive) });
	}

	public void setMultiplayerAuthority(long id) {
		setMultiplayerAuthority(id, true);
	}

	public int getMultiplayerAuthority() {
		return (int) super.call("get_multiplayer_authority");
	}

	public boolean isMultiplayerAuthority() {
		return (boolean) super.call("is_multiplayer_authority");
	}

	public void rpcConfig(String method, java.lang.Object config) {
		super.call("rpc_config", new java.lang.Object[] { (java.lang.Object) method, (java.lang.Object) config });
	}

	public java.lang.Object getNodeRpcConfig() {
		return (java.lang.Object) super.call("get_node_rpc_config");
	}

	public String atr(String message, String context) {
		return (String) super.call("atr", new java.lang.Object[] { (java.lang.Object) message, (java.lang.Object) context });
	}

	public String atr(String message) {
		return atr(message, "");
	}

	public String atrN(String message, String plural_message, long n, String context) {
		return (String) super.call("atr_n", new java.lang.Object[] { (java.lang.Object) message, (java.lang.Object) plural_message, java.lang.Long.valueOf(n), (java.lang.Object) context });
	}

	public String atrN(String message, String plural_message, long n) {
		return atrN(message, plural_message, n, "");
	}

	public int rpc(String method) {
		return (int) super.call("rpc", new java.lang.Object[] { (java.lang.Object) method });
	}

	public int rpcId(long peer_id, String method) {
		return (int) super.call("rpc_id", new java.lang.Object[] { java.lang.Long.valueOf(peer_id), (java.lang.Object) method });
	}

	public void updateConfigurationWarnings() {
		super.call("update_configuration_warnings");
	}

	public java.lang.Object callDeferredThreadGroup(String method) {
		return (java.lang.Object) super.call("call_deferred_thread_group", new java.lang.Object[] { (java.lang.Object) method });
	}

	public void setDeferredThreadGroup(String property, java.lang.Object value) {
		super.call("set_deferred_thread_group", new java.lang.Object[] { (java.lang.Object) property, (java.lang.Object) value });
	}

	public void notifyDeferredThreadGroup(long what) {
		super.call("notify_deferred_thread_group", new java.lang.Object[] { java.lang.Long.valueOf(what) });
	}

	public java.lang.Object callThreadSafe(String method) {
		return (java.lang.Object) super.call("call_thread_safe", new java.lang.Object[] { (java.lang.Object) method });
	}

	public void setThreadSafe(String property, java.lang.Object value) {
		super.call("set_thread_safe", new java.lang.Object[] { (java.lang.Object) property, (java.lang.Object) value });
	}

	public void notifyThreadSafe(long what) {
		super.call("notify_thread_safe", new java.lang.Object[] { java.lang.Long.valueOf(what) });
	}

	public String getName() {
		return (String) super.call("get_name", new java.lang.Object[0]);
	}

	public void setName(String value) {
		super.call("set_name", new java.lang.Object[] { (java.lang.Object) value });
	}

	public boolean isUniqueNameInOwner() {
		return (boolean) super.call("is_unique_name_in_owner", new java.lang.Object[0]);
	}

	public void setUniqueNameInOwner(boolean value) {
		super.call("set_unique_name_in_owner", new java.lang.Object[] { java.lang.Boolean.valueOf(value) });
	}

	public String getSceneFilePath() {
		return (String) super.call("get_scene_file_path", new java.lang.Object[0]);
	}

	public void setSceneFilePath(String value) {
		super.call("set_scene_file_path", new java.lang.Object[] { (java.lang.Object) value });
	}

	public Node getOwner() {
		return (Node) super.call("get_owner", new java.lang.Object[0]);
	}

	public void setOwner(Node value) {
		super.call("set_owner", new java.lang.Object[] { (java.lang.Object) value });
	}

	public MultiplayerAPI getMultiplayer() {
		return (MultiplayerAPI) super.call("get_multiplayer", new java.lang.Object[0]);
	}

	public void setMultiplayer(MultiplayerAPI value) {
		super.call("set_multiplayer", new java.lang.Object[] { (java.lang.Object) value });
	}

	public long getProcessMode() {
		return (long) super.call("get_process_mode", new java.lang.Object[0]);
	}

	public void setProcessMode(long value) {
		super.call("set_process_mode", new java.lang.Object[] { java.lang.Long.valueOf(value) });
	}

	public long getProcessPriority() {
		return (long) super.call("get_process_priority", new java.lang.Object[0]);
	}

	public void setProcessPriority(long value) {
		super.call("set_process_priority", new java.lang.Object[] { java.lang.Long.valueOf(value) });
	}

	public long getProcessPhysicsPriority() {
		return (long) super.call("get_physics_process_priority", new java.lang.Object[0]);
	}

	public void setProcessPhysicsPriority(long value) {
		super.call("set_physics_process_priority", new java.lang.Object[] { java.lang.Long.valueOf(value) });
	}

	public long getProcessThreadGroup() {
		return (long) super.call("get_process_thread_group", new java.lang.Object[0]);
	}

	public void setProcessThreadGroup(long value) {
		super.call("set_process_thread_group", new java.lang.Object[] { java.lang.Long.valueOf(value) });
	}

	public long getProcessThreadGroupOrder() {
		return (long) super.call("get_process_thread_group_order", new java.lang.Object[0]);
	}

	public void setProcessThreadGroupOrder(long value) {
		super.call("set_process_thread_group_order", new java.lang.Object[] { java.lang.Long.valueOf(value) });
	}

	public long getProcessThreadMessages() {
		return (long) super.call("get_process_thread_messages", new java.lang.Object[0]);
	}

	public void setProcessThreadMessages(long value) {
		super.call("set_process_thread_messages", new java.lang.Object[] { java.lang.Long.valueOf(value) });
	}

	public long getPhysicsInterpolationMode() {
		return (long) super.call("get_physics_interpolation_mode", new java.lang.Object[0]);
	}

	public void setPhysicsInterpolationMode(long value) {
		super.call("set_physics_interpolation_mode", new java.lang.Object[] { java.lang.Long.valueOf(value) });
	}

	public long getAutoTranslateMode() {
		return (long) super.call("get_auto_translate_mode", new java.lang.Object[0]);
	}

	public void setAutoTranslateMode(long value) {
		super.call("set_auto_translate_mode", new java.lang.Object[] { java.lang.Long.valueOf(value) });
	}

	public String getEditorDescription() {
		return (String) super.call("get_editor_description", new java.lang.Object[0]);
	}

	public void setEditorDescription(String value) {
		super.call("set_editor_description", new java.lang.Object[] { (java.lang.Object) value });
	}

	public static Node create() {
		GodotStringName name = GodotStringName.fromJavaString(GODOT_CLASS_NAME);
		MemorySegment ptr = Bridge.callPtr(ApiIndex.CLASSDB_CONSTRUCT_OBJECT, name.segment());
		return new Node(ptr);
	}

	@Override
	public String getGodotClassName() {
		return GODOT_CLASS_NAME;
	}

	@Override
	protected Godot.HashResult resolveMethodHash(String methodName) {
		Long hash = METHOD_HASHES.get(methodName);
		if (hash != null) return new Godot.HashResult(hash, GODOT_CLASS_NAME);
		return super.resolveMethodHash(methodName);
	}

	@SuppressWarnings("unchecked")
	public <T extends Object> T getNodeAs(String path, Class<T> type) {
		Node node = getNode(path);
		if (node == null) return null;
		return (T) node;
	}

	public enum ProcessMode {
		PROCESS_MODE_INHERIT(0),

		PROCESS_MODE_PAUSABLE(1),

		PROCESS_MODE_WHEN_PAUSED(2),

		PROCESS_MODE_ALWAYS(3),

		PROCESS_MODE_DISABLED(4);

		public final int value;

		ProcessMode(int value) {
			this.value = value;
		}
	}

	public enum ProcessThreadGroup {
		PROCESS_THREAD_GROUP_INHERIT(0),

		PROCESS_THREAD_GROUP_MAIN_THREAD(1),

		PROCESS_THREAD_GROUP_SUB_THREAD(2);

		public final int value;

		ProcessThreadGroup(int value) {
			this.value = value;
		}
	}

	public enum ProcessThreadMessages {
		FLAG_PROCESS_THREAD_MESSAGES(1L),

		FLAG_PROCESS_THREAD_MESSAGES_PHYSICS(2L),

		FLAG_PROCESS_THREAD_MESSAGES_ALL(3L);

		public final long value;

		ProcessThreadMessages(long value) {
			this.value = value;
		}
	}

	public enum PhysicsInterpolationMode {
		PHYSICS_INTERPOLATION_MODE_INHERIT(0),

		PHYSICS_INTERPOLATION_MODE_ON(1),

		PHYSICS_INTERPOLATION_MODE_OFF(2);

		public final int value;

		PhysicsInterpolationMode(int value) {
			this.value = value;
		}
	}

	public enum DuplicateFlags {
		DUPLICATE_SIGNALS(1),

		DUPLICATE_GROUPS(2),

		DUPLICATE_SCRIPTS(4),

		DUPLICATE_USE_INSTANTIATION(8),

		DUPLICATE_INTERNAL_STATE(16),

		DUPLICATE_DEFAULT(15);

		public final int value;

		DuplicateFlags(int value) {
			this.value = value;
		}
	}

	public enum InternalMode {
		INTERNAL_MODE_DISABLED(0),

		INTERNAL_MODE_FRONT(1),

		INTERNAL_MODE_BACK(2);

		public final int value;

		InternalMode(int value) {
			this.value = value;
		}
	}

	public enum AutoTranslateMode {
		AUTO_TRANSLATE_MODE_INHERIT(0),

		AUTO_TRANSLATE_MODE_ALWAYS(1),

		AUTO_TRANSLATE_MODE_DISABLED(2);

		public final int value;

		AutoTranslateMode(int value) {
			this.value = value;
		}
	}
}
