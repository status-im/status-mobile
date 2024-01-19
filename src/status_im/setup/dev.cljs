(ns status-im.setup.dev
  (:require
    ["react-native" :refer (DevSettings LogBox)]
    [react-native.platform :as platform]
    [reagent.impl.template :as reagent.template]
    [status-im.setup.schema :as schema]
    [utils.re-frame :as rf]))

;; Ignore all logs, because there are lots of temporary warnings when developing and hot reloading
(.ignoreAllLogs LogBox)

;; Only ignore warnings/errors that cannot be fixed for the time being.
;; When you add a warning to be ignored explain below why it is ignored and how it can be fixed.
;; When a warning is fixed make sure to remove it from here.
#_(.ignoreLogs ^js LogBox
               (clj->js ["undefined is not an object (evaluating 'e.message')"
                         "Cannot read property 'message' of undefined"
                         "InternalError Metro has encountered an error"
                         "undefined Unable to resolve module `parse-svg-path`"
                         "group12"
                         "Setting a timer for a long period of time"]))

;; List of ignored warnings/errors:
;; 1. "evaluating 'e.message'": Not sure why this error is happening, but it is coming from here
;; @walletconnect/jsonrpc-utils/dist/esm/error.js parseConnectionError() method
;; 2. "Cannot read property 'message' of undefined": same as 1, but for Android
;; 3. "InternalError Metro has encountered an error": an error that happens when losing connection to
;; metro, can be safely ignored
;; 4. "undefined Unable to resolve module `parse-svg-path`": an error that happens when losing
;; connection
;; to metro, can be safely ignored
;; 5. "group12": referring to the group-icon size 12x12. Currently, it is not available. When the design
;; team adds it to the
;; icon set it will be added to project.
;; 6. Setting a timer for a long period of time. Not sure why this error is happening
(set!
  reagent.template/prop-name-cache
  #js
   {:class                             "className"
    :for                               "htmlFor"
    :charset                           "charSet"
    :value                             "value"
    :style                             "style"
    :flex                              "flex"
    :background-color                  "backgroundColor"
    :flex-direction                    "flexDirection"
    :position                          "position"
    :overflow                          "overflow"
    :top                               "top"
    :bottom                            "bottom"
    :left                              "left"
    :right                             "right"
    :on-layout                         "onLayout"
    :blur-amount                       "blurAmount"
    :blur-radius                       "blurRadius"
    :blur-type                         "blurType"
    :overlay-color                     "overlayColor"
    :gesture                           "gesture"
    :width                             "width"
    :margin-left                       "marginLeft"
    :padding-top                       "paddingTop"
    :height                            "height"
    :resize-mode                       "resizeMode"
    :resize-method                     "resizeMethod"
    :margin-top                        "marginTop"
    :source                            "source"
    :padding-horizontal                "paddingHorizontal"
    :font-family                       "fontFamily"
    :font-size                         "fontSize"
    :line-height                       "lineHeight"
    :letter-spacing                    "letterSpacing"
    :text-align                        "textAlign"
    :color                             "color"
    :margin-right                      "marginRight"
    :border-radius                     "borderRadius"
    :behavior                          "behavior"
    :keyboardVerticalOffset            "keyboardVerticalOffset"
    :justify-content                   "justifyContent"
    :margin-bottom                     "marginBottom"
    :keyboard-should-persist-taps      "keyboardShouldPersistTaps"
    :disabled                          "disabled"
    :accessibility-label               "accessibilityLabel"
    :on-press-in                       "onPressIn"
    :on-press-out                      "onPressOut"
    :on-press                          "onPress"
    :on-long-press                     "onLongPress"
    :align-items                       "alignItems"
    :padding-right                     "paddingRight"
    :border-width                      "borderWidth"
    :padding-left                      "paddingLeft"
    :border-color                      "borderColor"
    :padding-bottom                    "paddingBottom"
    :key                               "key"
    :tint-color                        "tintColor"
    :holes                             "holes"
    :border-top-right-radius           "borderTopRightRadius"
    :border-bottom-left-radius         "borderBottomLeftRadius"
    :border-top-left-radius            "borderTopLeftRadius"
    :border-bottom-right-radius        "borderBottomRightRadius"
    :uri                               "uri"
    :number-of-lines                   "numberOfLines"
    :opacity                           "opacity"
    :auto-complete                     "autoComplete"
    :auto-capitalize                   "autoCapitalize"
    :placeholder                       "placeholder"
    :editable                          "editable"
    :on-focus                          "onFocus"
    :default-value                     "defaultValue"
    :on-blur                           "onBlur"
    :cursor-color                      "cursorColor"
    :placeholder-text-color            "placeholderTextColor"
    :on-change-text                    "onChangeText"
    :auto-focus                        "autoFocus"
    :secure-text-entry                 "secureTextEntry"
    :keyboard-appearance               "keyboardAppearance"
    :pointerEvents                     "pointerEvents"
    :animating                         "animating"
    :renderItem                        "renderItem"
    :ref                               "ref"
    :num-columns                       "numColumns"
    :ListHeaderComponent               "ListHeaderComponent"
    :content-container-style           "contentContainerStyle"
    :keyExtractor                      "keyExtractor"
    :column-wrapper-style              "columnWrapperStyle"
    :margin-horizontal                 "marginHorizontal"
    :content-inset-adjustment-behavior "contentInsetAdjustmentBehavior"
    :data                              "data"
    :colors                            "colors"
    :start                             "start"
    :x                                 "x"
    :y                                 "y"
    :end                               "end"
    :padding                           "padding"
    :on-error                          "onError"
    :on-load                           "onLoad"
    :z-index                           "zIndex"
    :test-ID                           "testID"
    :on-scroll                         "onScroll"
    :get-item-layout                   "getItemLayout"
    :scroll-event-throttle             "scrollEventThrottle"
    :on-end-reached                    "onEndReached"
    :padding-vertical                  "paddingVertical"
    :underlay-color                    "underlayColor"
    :flex-wrap                         "flexWrap"
    :flex-shrink                       "flexShrink"
    :monospace                         "monospace"
    :ellipsize-mode                    "ellipsizeMode"
    :shadow-offset                     "shadowOffset"
    :shadow-color                      "shadowColor"
    :elevation                         "elevation"
    :shadow-opacity                    "shadowOpacity"
    :shadow-radius                     "shadowRadius"
    :pointer-events                    "pointerEvents"
    :active-opacity                    "activeOpacity"
    :hit-slop                          "hitSlop"
    :max-width                         "maxWidth"
    :icon-color                        "iconColor"
    :display                           "display"
    :flex-grow                         "flexGrow"
    :d                                 "d"
    :fill                              "fill"
    :viewBox                           "viewBox"
    :transform                         "transform"
    :scale                             "scale"
    :stroke                            "stroke"
    :stroke-width                      "strokeWidth"
    :view-box                          "viewBox"
    :gap                               "gap"
    :accessible                        "accessible"
    :keyboard-vertical-offset          "keyboardVerticalOffset"
    :scroll-indicator-insets           "scrollIndicatorInsets"
    :on-content-size-change            "onContentSizeChange"
    :render-data                       "renderData"
    :theme                             "theme"
    :context                           "context"
    :in-pinned-view?                   "inPinnedView?"
    :community-admin?                  "communityAdmin?"
    :current-public-key                "currentPublicKey"
    :able-to-send-message?             "ableToSendMessage?"
    :message-pin-enabled               "messagePinEnabled"
    :community?                        "community?"
    :group-admin?                      "groupAdmin?"
    :group-chat                        "groupChat"
    :public?                           "public?"
    :chat-id                           "chatId"
    :can-delete-message-for-everyone?  "canDeleteMessageForEveryone?"
    :space-keeper                      "spaceKeeper"
    :keyboard-shown?                   "keyboardShown?"
    :insets                            "insets"
    :on-scroll-begin-drag              "onScrollBeginDrag"
    :ListFooterComponent               "ListFooterComponent"
    :on-momentum-scroll-end            "onMomentumScrollEnd"
    :on-momentum-scroll-begin          "onMomentumScrollBegin"
    :bounces                           "bounces"
    :keyboard-dismiss-mode             "keyboardDismissMode"
    :scroll-enabled                    "scrollEnabled"
    :on-viewable-items-changed         "onViewableItemsChanged"
    :inverted                          "inverted"
    :on-scroll-to-index-failed         "onScrollToIndexFailed"
    :max-height                        "maxHeight"
    :margin-vertical                   "marginVertical"
    :text-transform                    "textTransform"
    :align-self                        "alignSelf"
    :mask-element                      "maskElement"
    :cursor-position                   "cursorPosition"
    :input-ref                         "inputRef"
    :menu-items                        "menuItems"
    :min-height                        "minHeight"
    :max-length                        "maxLength"
    :multiline                         "multiline"
    :text-align-vertical               "textAlignVertical"
    :on-selection-change               "onSelectionChange"
    :on-selection                      "onSelection"
    :max-font-size-multiplier          "maxFontSizeMultiplier"
    :snap-to-interval                  "snapToInterval"
    :horizontal                        "horizontal"
    :shows-horizontal-scroll-indicator "showsHorizontalScrollIndicator"
    :on-clear                          "onClear"
    :loading-message                   "loadingMessage"
    :container-style                   "containerStyle"
    :deceleration-rate                 "decelerationRate"
    :ItemSeparatorComponent            "ItemSeparatorComponent"
    :native-ID                         "nativeID"
    :customization-color               "customizationColor"
    :shows-vertical-scroll-indicator   "showsVerticalScrollIndicator"
    :ellipis-mode                      "ellipisMode"
    :border-style                      "borderStyle"
    :ignore-offset                     "ignoreOffset"
    :important-for-accessibility       "importantForAccessibility"
    :return-key-type                   "returnKeyType"
    :margin                            "margin"
    :auto-complete-type                "autoCompleteType"
    :underline-color-android           "underlineColorAndroid"
    :clear-button-mode                 "clearButtonMode"
    :on-submit-editing                 "onSubmitEditing"
    :keyboard-type                     "keyboardType"
    :auto-correct                      "autoCorrect"
    :render-fn                         "renderFn"
    :header                            "header"
    :ListEmptyComponent                "ListEmptyComponent"
    :key-fn                            "keyFn"
    :empty-component                   "emptyComponent"
    :border-top-width                  "borderTopWidth"
    :border-top-color                  "borderTopColor"})


(defn setup
  []
  (rf/set-mergeable-keys #{:filters/load-filters
                           :pairing/set-installation-metadata
                           :fx
                           :dispatch-n
                           :legacy.status-im.ens.core/verify-names
                           :shh/send-direct-message
                           :shh/remove-filter
                           :transport/confirm-messages-processed
                           :group-chats/extract-membership-signature
                           :utils/dispatch-later
                           :json-rpc/call})
  (when ^:boolean js/goog.DEBUG
    (schema/setup!)
    (when (and platform/ios? DevSettings)
      ;;on Android this method doesn't work
      (when-let [nm (.-_nativeModule DevSettings)]
        ;;there is a bug in RN, so we have to enable it first and then disable
        (.setHotLoadingEnabled ^js nm true)
        (js/setTimeout #(.setHotLoadingEnabled ^js nm false) 1000)))))
