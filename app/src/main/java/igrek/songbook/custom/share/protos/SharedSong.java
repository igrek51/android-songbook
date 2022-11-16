// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: shared_song.proto

package igrek.songbook.custom.share.protos;

public final class SharedSong {
	private SharedSong() {
	}
	
	public static void registerAllExtensions(com.google.protobuf.ExtensionRegistryLite registry) {
	}
	
	public static void registerAllExtensions(com.google.protobuf.ExtensionRegistry registry) {
		registerAllExtensions((com.google.protobuf.ExtensionRegistryLite) registry);
	}
	
	public interface SharedSongDtoOrBuilder extends
			// @@protoc_insertion_point(interface_extends:igrek.songbook.custom.share.protos.SharedSongDto)
			com.google.protobuf.MessageOrBuilder {
		
		/**
		 * <code>string title = 1;</code>
		 * @return The title.
		 */
		java.lang.String getTitle();
		
		/**
		 * <code>string title = 1;</code>
		 * @return The bytes for title.
		 */
		com.google.protobuf.ByteString getTitleBytes();
		
		/**
		 * <code>string content = 2;</code>
		 * @return The content.
		 */
		java.lang.String getContent();
		
		/**
		 * <code>string content = 2;</code>
		 * @return The bytes for content.
		 */
		com.google.protobuf.ByteString getContentBytes();
		
		/**
		 * <code>optional string customCategory = 3;</code>
		 * @return Whether the customCategory field is set.
		 */
		boolean hasCustomCategory();
		
		/**
		 * <code>optional string customCategory = 3;</code>
		 * @return The customCategory.
		 */
		java.lang.String getCustomCategory();
		
		/**
		 * <code>optional string customCategory = 3;</code>
		 * @return The bytes for customCategory.
		 */
		com.google.protobuf.ByteString getCustomCategoryBytes();
		
		/**
		 * <code>optional int64 chordsNotation = 4;</code>
		 * @return Whether the chordsNotation field is set.
		 */
		boolean hasChordsNotation();
		
		/**
		 * <code>optional int64 chordsNotation = 4;</code>
		 * @return The chordsNotation.
		 */
		long getChordsNotation();
	}
	/**
	 * Protobuf type {@code igrek.songbook.custom.share.protos.SharedSongDto}
	 */
	public static final class SharedSongDto extends com.google.protobuf.GeneratedMessageV3 implements
			// @@protoc_insertion_point(message_implements:igrek.songbook.custom.share.protos.SharedSongDto)
			SharedSongDtoOrBuilder {
		private static final long serialVersionUID = 0L;
		
		// Use SharedSongDto.newBuilder() to construct.
		private SharedSongDto(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
			super(builder);
		}
		
		private SharedSongDto() {
			title_ = "";
			content_ = "";
			customCategory_ = "";
		}
		
		@java.lang.Override
		@SuppressWarnings({"unused"})
		protected java.lang.Object newInstance(UnusedPrivateParameter unused) {
			return new SharedSongDto();
		}
		
		@java.lang.Override
		public final com.google.protobuf.UnknownFieldSet getUnknownFields() {
			return this.unknownFields;
		}
		
		private SharedSongDto(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
			this();
			if (extensionRegistry == null) {
				throw new java.lang.NullPointerException();
			}
			int mutable_bitField0_ = 0;
			com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder();
			try {
				boolean done = false;
				while (!done) {
					int tag = input.readTag();
					switch (tag) {
						case 0:
							done = true;
							break;
						case 10: {
							java.lang.String s = input.readStringRequireUtf8();
							
							title_ = s;
							break;
						}
						case 18: {
							java.lang.String s = input.readStringRequireUtf8();
							
							content_ = s;
							break;
						}
						case 26: {
							java.lang.String s = input.readStringRequireUtf8();
							bitField0_ |= 0x00000001;
							customCategory_ = s;
							break;
						}
						case 32: {
							bitField0_ |= 0x00000002;
							chordsNotation_ = input.readInt64();
							break;
						}
						default: {
							if (!parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
								done = true;
							}
							break;
						}
					}
				}
			} catch (com.google.protobuf.InvalidProtocolBufferException e) {
				throw e.setUnfinishedMessage(this);
			} catch (java.io.IOException e) {
				throw new com.google.protobuf.InvalidProtocolBufferException(e).setUnfinishedMessage(this);
			} finally {
				this.unknownFields = unknownFields.build();
				makeExtensionsImmutable();
			}
		}
		
		public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
			return igrek.songbook.custom.share.protos.SharedSong.internal_static_igrek_songbook_custom_share_protos_SharedSongDto_descriptor;
		}
		
		@java.lang.Override
		protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
			return igrek.songbook.custom.share.protos.SharedSong.internal_static_igrek_songbook_custom_share_protos_SharedSongDto_fieldAccessorTable.ensureFieldAccessorsInitialized(igrek.songbook.custom.share.protos.SharedSong.SharedSongDto.class, igrek.songbook.custom.share.protos.SharedSong.SharedSongDto.Builder.class);
		}
		
		private int bitField0_;
		public static final int TITLE_FIELD_NUMBER = 1;
		private volatile java.lang.Object title_;
		
		/**
		 * <code>string title = 1;</code>
		 * @return The title.
		 */
		@java.lang.Override
		public java.lang.String getTitle() {
			java.lang.Object ref = title_;
			if (ref instanceof java.lang.String) {
				return (java.lang.String) ref;
			} else {
				com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
				java.lang.String s = bs.toStringUtf8();
				title_ = s;
				return s;
			}
		}
		
		/**
		 * <code>string title = 1;</code>
		 * @return The bytes for title.
		 */
		@java.lang.Override
		public com.google.protobuf.ByteString getTitleBytes() {
			java.lang.Object ref = title_;
			if (ref instanceof java.lang.String) {
				com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
				title_ = b;
				return b;
			} else {
				return (com.google.protobuf.ByteString) ref;
			}
		}
		
		public static final int CONTENT_FIELD_NUMBER = 2;
		private volatile java.lang.Object content_;
		
		/**
		 * <code>string content = 2;</code>
		 * @return The content.
		 */
		@java.lang.Override
		public java.lang.String getContent() {
			java.lang.Object ref = content_;
			if (ref instanceof java.lang.String) {
				return (java.lang.String) ref;
			} else {
				com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
				java.lang.String s = bs.toStringUtf8();
				content_ = s;
				return s;
			}
		}
		
		/**
		 * <code>string content = 2;</code>
		 * @return The bytes for content.
		 */
		@java.lang.Override
		public com.google.protobuf.ByteString getContentBytes() {
			java.lang.Object ref = content_;
			if (ref instanceof java.lang.String) {
				com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
				content_ = b;
				return b;
			} else {
				return (com.google.protobuf.ByteString) ref;
			}
		}
		
		public static final int CUSTOMCATEGORY_FIELD_NUMBER = 3;
		private volatile java.lang.Object customCategory_;
		
		/**
		 * <code>optional string customCategory = 3;</code>
		 * @return Whether the customCategory field is set.
		 */
		@java.lang.Override
		public boolean hasCustomCategory() {
			return ((bitField0_ & 0x00000001) != 0);
		}
		
		/**
		 * <code>optional string customCategory = 3;</code>
		 * @return The customCategory.
		 */
		@java.lang.Override
		public java.lang.String getCustomCategory() {
			java.lang.Object ref = customCategory_;
			if (ref instanceof java.lang.String) {
				return (java.lang.String) ref;
			} else {
				com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
				java.lang.String s = bs.toStringUtf8();
				customCategory_ = s;
				return s;
			}
		}
		
		/**
		 * <code>optional string customCategory = 3;</code>
		 * @return The bytes for customCategory.
		 */
		@java.lang.Override
		public com.google.protobuf.ByteString getCustomCategoryBytes() {
			java.lang.Object ref = customCategory_;
			if (ref instanceof java.lang.String) {
				com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
				customCategory_ = b;
				return b;
			} else {
				return (com.google.protobuf.ByteString) ref;
			}
		}
		
		public static final int CHORDSNOTATION_FIELD_NUMBER = 4;
		private long chordsNotation_;
		
		/**
		 * <code>optional int64 chordsNotation = 4;</code>
		 * @return Whether the chordsNotation field is set.
		 */
		@java.lang.Override
		public boolean hasChordsNotation() {
			return ((bitField0_ & 0x00000002) != 0);
		}
		
		/**
		 * <code>optional int64 chordsNotation = 4;</code>
		 * @return The chordsNotation.
		 */
		@java.lang.Override
		public long getChordsNotation() {
			return chordsNotation_;
		}
		
		private byte memoizedIsInitialized = -1;
		
		@java.lang.Override
		public final boolean isInitialized() {
			byte isInitialized = memoizedIsInitialized;
			if (isInitialized == 1)
				return true;
			if (isInitialized == 0)
				return false;
			
			memoizedIsInitialized = 1;
			return true;
		}
		
		@java.lang.Override
		public void writeTo(com.google.protobuf.CodedOutputStream output) throws java.io.IOException {
			if (!getTitleBytes().isEmpty()) {
				com.google.protobuf.GeneratedMessageV3.writeString(output, 1, title_);
			}
			if (!getContentBytes().isEmpty()) {
				com.google.protobuf.GeneratedMessageV3.writeString(output, 2, content_);
			}
			if (((bitField0_ & 0x00000001) != 0)) {
				com.google.protobuf.GeneratedMessageV3.writeString(output, 3, customCategory_);
			}
			if (((bitField0_ & 0x00000002) != 0)) {
				output.writeInt64(4, chordsNotation_);
			}
			unknownFields.writeTo(output);
		}
		
		@java.lang.Override
		public int getSerializedSize() {
			int size = memoizedSize;
			if (size != -1)
				return size;
			
			size = 0;
			if (!getTitleBytes().isEmpty()) {
				size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, title_);
			}
			if (!getContentBytes().isEmpty()) {
				size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, content_);
			}
			if (((bitField0_ & 0x00000001) != 0)) {
				size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, customCategory_);
			}
			if (((bitField0_ & 0x00000002) != 0)) {
				size += com.google.protobuf.CodedOutputStream.computeInt64Size(4, chordsNotation_);
			}
			size += unknownFields.getSerializedSize();
			memoizedSize = size;
			return size;
		}
		
		@java.lang.Override
		public boolean equals(final java.lang.Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof igrek.songbook.custom.share.protos.SharedSong.SharedSongDto)) {
				return super.equals(obj);
			}
			igrek.songbook.custom.share.protos.SharedSong.SharedSongDto other = (igrek.songbook.custom.share.protos.SharedSong.SharedSongDto) obj;
			
			if (!getTitle().equals(other.getTitle()))
				return false;
			if (!getContent().equals(other.getContent()))
				return false;
			if (hasCustomCategory() != other.hasCustomCategory())
				return false;
			if (hasCustomCategory()) {
				if (!getCustomCategory().equals(other.getCustomCategory()))
					return false;
			}
			if (hasChordsNotation() != other.hasChordsNotation())
				return false;
			if (hasChordsNotation()) {
				if (getChordsNotation() != other.getChordsNotation())
					return false;
			}
			return unknownFields.equals(other.unknownFields);
		}
		
		@java.lang.Override
		public int hashCode() {
			if (memoizedHashCode != 0) {
				return memoizedHashCode;
			}
			int hash = 41;
			hash = (19 * hash) + getDescriptor().hashCode();
			hash = (37 * hash) + TITLE_FIELD_NUMBER;
			hash = (53 * hash) + getTitle().hashCode();
			hash = (37 * hash) + CONTENT_FIELD_NUMBER;
			hash = (53 * hash) + getContent().hashCode();
			if (hasCustomCategory()) {
				hash = (37 * hash) + CUSTOMCATEGORY_FIELD_NUMBER;
				hash = (53 * hash) + getCustomCategory().hashCode();
			}
			if (hasChordsNotation()) {
				hash = (37 * hash) + CHORDSNOTATION_FIELD_NUMBER;
				hash = (53 * hash) + com.google.protobuf.Internal.hashLong(getChordsNotation());
			}
			hash = (29 * hash) + unknownFields.hashCode();
			memoizedHashCode = hash;
			return hash;
		}
		
		public static igrek.songbook.custom.share.protos.SharedSong.SharedSongDto parseFrom(java.nio.ByteBuffer data) throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}
		
		public static igrek.songbook.custom.share.protos.SharedSong.SharedSongDto parseFrom(java.nio.ByteBuffer data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}
		
		public static igrek.songbook.custom.share.protos.SharedSong.SharedSongDto parseFrom(com.google.protobuf.ByteString data) throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}
		
		public static igrek.songbook.custom.share.protos.SharedSong.SharedSongDto parseFrom(com.google.protobuf.ByteString data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}
		
		public static igrek.songbook.custom.share.protos.SharedSong.SharedSongDto parseFrom(byte[] data) throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data);
		}
		
		public static igrek.songbook.custom.share.protos.SharedSong.SharedSongDto parseFrom(byte[] data, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
			return PARSER.parseFrom(data, extensionRegistry);
		}
		
		public static igrek.songbook.custom.share.protos.SharedSong.SharedSongDto parseFrom(java.io.InputStream input) throws java.io.IOException {
			return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
		}
		
		public static igrek.songbook.custom.share.protos.SharedSong.SharedSongDto parseFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
		}
		
		public static igrek.songbook.custom.share.protos.SharedSong.SharedSongDto parseDelimitedFrom(java.io.InputStream input) throws java.io.IOException {
			return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
		}
		
		public static igrek.songbook.custom.share.protos.SharedSong.SharedSongDto parseDelimitedFrom(java.io.InputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return com.google.protobuf.GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
		}
		
		public static igrek.songbook.custom.share.protos.SharedSong.SharedSongDto parseFrom(com.google.protobuf.CodedInputStream input) throws java.io.IOException {
			return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input);
		}
		
		public static igrek.songbook.custom.share.protos.SharedSong.SharedSongDto parseFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
			return com.google.protobuf.GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
		}
		
		@java.lang.Override
		public Builder newBuilderForType() {
			return newBuilder();
		}
		
		public static Builder newBuilder() {
			return DEFAULT_INSTANCE.toBuilder();
		}
		
		public static Builder newBuilder(igrek.songbook.custom.share.protos.SharedSong.SharedSongDto prototype) {
			return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
		}
		
		@java.lang.Override
		public Builder toBuilder() {
			return this == DEFAULT_INSTANCE ? new Builder() : new Builder().mergeFrom(this);
		}
		
		@java.lang.Override
		protected Builder newBuilderForType(com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
			Builder builder = new Builder(parent);
			return builder;
		}
		
		/**
		 * Protobuf type {@code igrek.songbook.custom.share.protos.SharedSongDto}
		 */
		public static final class Builder extends com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
				// @@protoc_insertion_point(builder_implements:igrek.songbook.custom.share.protos.SharedSongDto)
				igrek.songbook.custom.share.protos.SharedSong.SharedSongDtoOrBuilder {
			public static final com.google.protobuf.Descriptors.Descriptor getDescriptor() {
				return igrek.songbook.custom.share.protos.SharedSong.internal_static_igrek_songbook_custom_share_protos_SharedSongDto_descriptor;
			}
			
			@java.lang.Override
			protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
				return igrek.songbook.custom.share.protos.SharedSong.internal_static_igrek_songbook_custom_share_protos_SharedSongDto_fieldAccessorTable.ensureFieldAccessorsInitialized(igrek.songbook.custom.share.protos.SharedSong.SharedSongDto.class, igrek.songbook.custom.share.protos.SharedSong.SharedSongDto.Builder.class);
			}
			
			// Construct using igrek.songbook.custom.share.protos.SharedSong.SharedSongDto.newBuilder()
			private Builder() {
				maybeForceBuilderInitialization();
			}
			
			private Builder(com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
				super(parent);
				maybeForceBuilderInitialization();
			}
			
			private void maybeForceBuilderInitialization() {
				if (com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders) {
				}
			}
			
			@java.lang.Override
			public Builder clear() {
				super.clear();
				title_ = "";
				
				content_ = "";
				
				customCategory_ = "";
				bitField0_ = (bitField0_ & ~0x00000001);
				chordsNotation_ = 0L;
				bitField0_ = (bitField0_ & ~0x00000002);
				return this;
			}
			
			@java.lang.Override
			public com.google.protobuf.Descriptors.Descriptor getDescriptorForType() {
				return igrek.songbook.custom.share.protos.SharedSong.internal_static_igrek_songbook_custom_share_protos_SharedSongDto_descriptor;
			}
			
			@java.lang.Override
			public igrek.songbook.custom.share.protos.SharedSong.SharedSongDto getDefaultInstanceForType() {
				return igrek.songbook.custom.share.protos.SharedSong.SharedSongDto.getDefaultInstance();
			}
			
			@java.lang.Override
			public igrek.songbook.custom.share.protos.SharedSong.SharedSongDto build() {
				igrek.songbook.custom.share.protos.SharedSong.SharedSongDto result = buildPartial();
				if (!result.isInitialized()) {
					throw newUninitializedMessageException(result);
				}
				return result;
			}
			
			@java.lang.Override
			public igrek.songbook.custom.share.protos.SharedSong.SharedSongDto buildPartial() {
				igrek.songbook.custom.share.protos.SharedSong.SharedSongDto result = new igrek.songbook.custom.share.protos.SharedSong.SharedSongDto(this);
				int from_bitField0_ = bitField0_;
				int to_bitField0_ = 0;
				result.title_ = title_;
				result.content_ = content_;
				if (((from_bitField0_ & 0x00000001) != 0)) {
					to_bitField0_ |= 0x00000001;
				}
				result.customCategory_ = customCategory_;
				if (((from_bitField0_ & 0x00000002) != 0)) {
					result.chordsNotation_ = chordsNotation_;
					to_bitField0_ |= 0x00000002;
				}
				result.bitField0_ = to_bitField0_;
				onBuilt();
				return result;
			}
			
			@java.lang.Override
			public Builder clone() {
				return super.clone();
			}
			
			@java.lang.Override
			public Builder setField(com.google.protobuf.Descriptors.FieldDescriptor field, java.lang.Object value) {
				return super.setField(field, value);
			}
			
			@java.lang.Override
			public Builder clearField(com.google.protobuf.Descriptors.FieldDescriptor field) {
				return super.clearField(field);
			}
			
			@java.lang.Override
			public Builder clearOneof(com.google.protobuf.Descriptors.OneofDescriptor oneof) {
				return super.clearOneof(oneof);
			}
			
			@java.lang.Override
			public Builder setRepeatedField(com.google.protobuf.Descriptors.FieldDescriptor field, int index, java.lang.Object value) {
				return super.setRepeatedField(field, index, value);
			}
			
			@java.lang.Override
			public Builder addRepeatedField(com.google.protobuf.Descriptors.FieldDescriptor field, java.lang.Object value) {
				return super.addRepeatedField(field, value);
			}
			
			@java.lang.Override
			public Builder mergeFrom(com.google.protobuf.Message other) {
				if (other instanceof igrek.songbook.custom.share.protos.SharedSong.SharedSongDto) {
					return mergeFrom((igrek.songbook.custom.share.protos.SharedSong.SharedSongDto) other);
				} else {
					super.mergeFrom(other);
					return this;
				}
			}
			
			public Builder mergeFrom(igrek.songbook.custom.share.protos.SharedSong.SharedSongDto other) {
				if (other == igrek.songbook.custom.share.protos.SharedSong.SharedSongDto.getDefaultInstance())
					return this;
				if (!other.getTitle().isEmpty()) {
					title_ = other.title_;
					onChanged();
				}
				if (!other.getContent().isEmpty()) {
					content_ = other.content_;
					onChanged();
				}
				if (other.hasCustomCategory()) {
					bitField0_ |= 0x00000001;
					customCategory_ = other.customCategory_;
					onChanged();
				}
				if (other.hasChordsNotation()) {
					setChordsNotation(other.getChordsNotation());
				}
				this.mergeUnknownFields(other.unknownFields);
				onChanged();
				return this;
			}
			
			@java.lang.Override
			public final boolean isInitialized() {
				return true;
			}
			
			@java.lang.Override
			public Builder mergeFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws java.io.IOException {
				igrek.songbook.custom.share.protos.SharedSong.SharedSongDto parsedMessage = null;
				try {
					parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
				} catch (com.google.protobuf.InvalidProtocolBufferException e) {
					parsedMessage = (igrek.songbook.custom.share.protos.SharedSong.SharedSongDto) e.getUnfinishedMessage();
					throw e.unwrapIOException();
				} finally {
					if (parsedMessage != null) {
						mergeFrom(parsedMessage);
					}
				}
				return this;
			}
			
			private int bitField0_;
			
			private java.lang.Object title_ = "";
			
			/**
			 * <code>string title = 1;</code>
			 * @return The title.
			 */
			public java.lang.String getTitle() {
				java.lang.Object ref = title_;
				if (!(ref instanceof java.lang.String)) {
					com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
					java.lang.String s = bs.toStringUtf8();
					title_ = s;
					return s;
				} else {
					return (java.lang.String) ref;
				}
			}
			
			/**
			 * <code>string title = 1;</code>
			 * @return The bytes for title.
			 */
			public com.google.protobuf.ByteString getTitleBytes() {
				java.lang.Object ref = title_;
				if (ref instanceof String) {
					com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
					title_ = b;
					return b;
				} else {
					return (com.google.protobuf.ByteString) ref;
				}
			}
			
			/**
			 * <code>string title = 1;</code>
			 * @param value The title to set.
			 * @return This builder for chaining.
			 */
			public Builder setTitle(java.lang.String value) {
				if (value == null) {
					throw new NullPointerException();
				}
				
				title_ = value;
				onChanged();
				return this;
			}
			
			/**
			 * <code>string title = 1;</code>
			 * @return This builder for chaining.
			 */
			public Builder clearTitle() {
				
				title_ = getDefaultInstance().getTitle();
				onChanged();
				return this;
			}
			
			/**
			 * <code>string title = 1;</code>
			 * @param value The bytes for title to set.
			 * @return This builder for chaining.
			 */
			public Builder setTitleBytes(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				checkByteStringIsUtf8(value);
				
				title_ = value;
				onChanged();
				return this;
			}
			
			private java.lang.Object content_ = "";
			
			/**
			 * <code>string content = 2;</code>
			 * @return The content.
			 */
			public java.lang.String getContent() {
				java.lang.Object ref = content_;
				if (!(ref instanceof java.lang.String)) {
					com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
					java.lang.String s = bs.toStringUtf8();
					content_ = s;
					return s;
				} else {
					return (java.lang.String) ref;
				}
			}
			
			/**
			 * <code>string content = 2;</code>
			 * @return The bytes for content.
			 */
			public com.google.protobuf.ByteString getContentBytes() {
				java.lang.Object ref = content_;
				if (ref instanceof String) {
					com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
					content_ = b;
					return b;
				} else {
					return (com.google.protobuf.ByteString) ref;
				}
			}
			
			/**
			 * <code>string content = 2;</code>
			 * @param value The content to set.
			 * @return This builder for chaining.
			 */
			public Builder setContent(java.lang.String value) {
				if (value == null) {
					throw new NullPointerException();
				}
				
				content_ = value;
				onChanged();
				return this;
			}
			
			/**
			 * <code>string content = 2;</code>
			 * @return This builder for chaining.
			 */
			public Builder clearContent() {
				
				content_ = getDefaultInstance().getContent();
				onChanged();
				return this;
			}
			
			/**
			 * <code>string content = 2;</code>
			 * @param value The bytes for content to set.
			 * @return This builder for chaining.
			 */
			public Builder setContentBytes(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				checkByteStringIsUtf8(value);
				
				content_ = value;
				onChanged();
				return this;
			}
			
			private java.lang.Object customCategory_ = "";
			
			/**
			 * <code>optional string customCategory = 3;</code>
			 * @return Whether the customCategory field is set.
			 */
			public boolean hasCustomCategory() {
				return ((bitField0_ & 0x00000001) != 0);
			}
			
			/**
			 * <code>optional string customCategory = 3;</code>
			 * @return The customCategory.
			 */
			public java.lang.String getCustomCategory() {
				java.lang.Object ref = customCategory_;
				if (!(ref instanceof java.lang.String)) {
					com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
					java.lang.String s = bs.toStringUtf8();
					customCategory_ = s;
					return s;
				} else {
					return (java.lang.String) ref;
				}
			}
			
			/**
			 * <code>optional string customCategory = 3;</code>
			 * @return The bytes for customCategory.
			 */
			public com.google.protobuf.ByteString getCustomCategoryBytes() {
				java.lang.Object ref = customCategory_;
				if (ref instanceof String) {
					com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8((java.lang.String) ref);
					customCategory_ = b;
					return b;
				} else {
					return (com.google.protobuf.ByteString) ref;
				}
			}
			
			/**
			 * <code>optional string customCategory = 3;</code>
			 * @param value The customCategory to set.
			 * @return This builder for chaining.
			 */
			public Builder setCustomCategory(java.lang.String value) {
				if (value == null) {
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000001;
				customCategory_ = value;
				onChanged();
				return this;
			}
			
			/**
			 * <code>optional string customCategory = 3;</code>
			 * @return This builder for chaining.
			 */
			public Builder clearCustomCategory() {
				bitField0_ = (bitField0_ & ~0x00000001);
				customCategory_ = getDefaultInstance().getCustomCategory();
				onChanged();
				return this;
			}
			
			/**
			 * <code>optional string customCategory = 3;</code>
			 * @param value The bytes for customCategory to set.
			 * @return This builder for chaining.
			 */
			public Builder setCustomCategoryBytes(com.google.protobuf.ByteString value) {
				if (value == null) {
					throw new NullPointerException();
				}
				checkByteStringIsUtf8(value);
				bitField0_ |= 0x00000001;
				customCategory_ = value;
				onChanged();
				return this;
			}
			
			private long chordsNotation_;
			
			/**
			 * <code>optional int64 chordsNotation = 4;</code>
			 * @return Whether the chordsNotation field is set.
			 */
			@java.lang.Override
			public boolean hasChordsNotation() {
				return ((bitField0_ & 0x00000002) != 0);
			}
			
			/**
			 * <code>optional int64 chordsNotation = 4;</code>
			 * @return The chordsNotation.
			 */
			@java.lang.Override
			public long getChordsNotation() {
				return chordsNotation_;
			}
			
			/**
			 * <code>optional int64 chordsNotation = 4;</code>
			 * @param value The chordsNotation to set.
			 * @return This builder for chaining.
			 */
			public Builder setChordsNotation(long value) {
				bitField0_ |= 0x00000002;
				chordsNotation_ = value;
				onChanged();
				return this;
			}
			
			/**
			 * <code>optional int64 chordsNotation = 4;</code>
			 * @return This builder for chaining.
			 */
			public Builder clearChordsNotation() {
				bitField0_ = (bitField0_ & ~0x00000002);
				chordsNotation_ = 0L;
				onChanged();
				return this;
			}
			
			@java.lang.Override
			public final Builder setUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
				return super.setUnknownFields(unknownFields);
			}
			
			@java.lang.Override
			public final Builder mergeUnknownFields(final com.google.protobuf.UnknownFieldSet unknownFields) {
				return super.mergeUnknownFields(unknownFields);
			}
			
			
			// @@protoc_insertion_point(builder_scope:igrek.songbook.custom.share.protos.SharedSongDto)
		}
		
		// @@protoc_insertion_point(class_scope:igrek.songbook.custom.share.protos.SharedSongDto)
		private static final igrek.songbook.custom.share.protos.SharedSong.SharedSongDto DEFAULT_INSTANCE;
		
		static {
			DEFAULT_INSTANCE = new igrek.songbook.custom.share.protos.SharedSong.SharedSongDto();
		}
		
		public static igrek.songbook.custom.share.protos.SharedSong.SharedSongDto getDefaultInstance() {
			return DEFAULT_INSTANCE;
		}
		
		private static final com.google.protobuf.Parser<SharedSongDto> PARSER = new com.google.protobuf.AbstractParser<SharedSongDto>() {
			@java.lang.Override
			public SharedSongDto parsePartialFrom(com.google.protobuf.CodedInputStream input, com.google.protobuf.ExtensionRegistryLite extensionRegistry) throws com.google.protobuf.InvalidProtocolBufferException {
				return new SharedSongDto(input, extensionRegistry);
			}
		};
		
		public static com.google.protobuf.Parser<SharedSongDto> parser() {
			return PARSER;
		}
		
		@java.lang.Override
		public com.google.protobuf.Parser<SharedSongDto> getParserForType() {
			return PARSER;
		}
		
		@java.lang.Override
		public igrek.songbook.custom.share.protos.SharedSong.SharedSongDto getDefaultInstanceForType() {
			return DEFAULT_INSTANCE;
		}
		
	}
	
	private static final com.google.protobuf.Descriptors.Descriptor internal_static_igrek_songbook_custom_share_protos_SharedSongDto_descriptor;
	private static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_igrek_songbook_custom_share_protos_SharedSongDto_fieldAccessorTable;
	
	public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
		return descriptor;
	}
	
	private static final com.google.protobuf.Descriptors.FileDescriptor descriptor;
	
	static {
		java.lang.String[] descriptorData = {
				"\n\021shared_song.proto\022\"igrek.songbook.cust" + "om.share.protos\"\217\001\n\rSharedSongDto\022\r\n\005tit" + "le\030\001 \001(\t\022\017\n\007content\030\002 \001(\t\022\033\n\016customCateg" + "ory\030\003 \001(\tH\000\210\001\001\022\033\n\016chordsNotation\030\004 \001(\003H\001" + "\210\001\001B\021\n\017_customCategoryB\021\n\017_chordsNotatio" + "nb\006proto3"
		};
		descriptor = com.google.protobuf.Descriptors.FileDescriptor.internalBuildGeneratedFileFrom(descriptorData, new com.google.protobuf.Descriptors.FileDescriptor[]{
		});
		internal_static_igrek_songbook_custom_share_protos_SharedSongDto_descriptor = getDescriptor().getMessageTypes()
				.get(0);
		internal_static_igrek_songbook_custom_share_protos_SharedSongDto_fieldAccessorTable = new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(internal_static_igrek_songbook_custom_share_protos_SharedSongDto_descriptor, new java.lang.String[]{
				"Title", "Content", "CustomCategory", "ChordsNotation", "CustomCategory",
				"ChordsNotation",
		});
	}
	
	// @@protoc_insertion_point(outer_class_scope)
}
