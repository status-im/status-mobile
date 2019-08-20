(ns status-im.ui.components.react
  (:require-macros [status-im.utils.views :as views])
  (:require [goog.object :as object]
            [reagent.core :as reagent]
            [status-im.ui.components.styles :as styles]
            [status-im.utils.utils :as utils]
            [status-im.utils.core :as utils.core]
            [status-im.utils.platform :as platform]
            [status-im.i18n :as i18n]
            [status-im.react-native.js-dependencies :as js-dependencies]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.typography :as typography]))

(defn get-react-property [name]
  (if js-dependencies/react-native
    (or (object/get js-dependencies/react-native name) {})
    #js {}))

(defn lazy-get-react-property [name]
  (let [react-property (atom nil)]
    (fn []
      (if @react-property
        @react-property
        (reset! react-property (get-react-property name))))))

(defn adapt-class [class]
  (when class
    (reagent/adapt-react-class class)))

(defn get-class [name]
  (let [react-class (atom nil)]
    (fn []
      (if @react-class
        @react-class
        (reset! react-class
                (adapt-class (get-react-property name)))))))

(def native-modules (.-NativeModules js-dependencies/react-native))
(def device-event-emitter (.-DeviceEventEmitter js-dependencies/react-native))

(defn dismiss-keyboard! [] ((js-dependencies/dismiss-keyboard)))

(def splash-screen (.-SplashScreen native-modules))

;; React Components

(def app-registry (get-react-property "AppRegistry"))
(def app-state (lazy-get-react-property "AppState"))
(def view ((get-class "View")))
(def safe-area-view (get-class "SafeAreaView"))
(def progress-bar (get-class "ProgressBarAndroid"))

(def status-bar-class (when-not platform/desktop? (get-react-property "StatusBar")))

(def scroll-view-class (get-class "ScrollView"))
(def web-view #_(get-class "WebView"))
(def keyboard-avoiding-view-class (get-class "KeyboardAvoidingView"))

(def refresh-control (get-class "RefreshControl"))

(def text-class ((get-class "Text")))
(def text-input-class (get-class "TextInput"))
(def image-class (get-class "Image"))
(def picker-obj (lazy-get-react-property "Picker"))
(defn picker-class [] (adapt-class (picker-obj)))
(defn picker-item-class [] (adapt-class (.-Item (picker-obj))))

(defn valid-source? [source]
  (or (not (map? source))
      (not (contains? source :uri))
      (and (contains? source :uri)
           (:uri source))))

(defn image [{:keys [source] :as props}]
  (when (valid-source? source)
    (let [source (if (fn? source) (source) source)]
      [(image-class) (assoc props :source source)])))

(def switch-class (get-class "Switch"))

(defn switch [props]
  [(switch-class) props])

(def touchable-highlight-class (get-class "TouchableHighlight"))
(def touchable-without-feedback-class (get-class "TouchableWithoutFeedback"))
(def touchable-opacity-class (get-class "TouchableOpacity"))
(def activity-indicator-class (get-class "ActivityIndicator"))

(defn activity-indicator [props]
  [(activity-indicator-class) props])

(def modal (get-class "Modal"))

(def pan-responder (lazy-get-react-property "PanResponder"))
(def animated (lazy-get-react-property "Animated"))

(defn animated-view-class []
  (reagent/adapt-react-class (.-View (animated))))

(defn animated-view [props & content]
  (vec (conj content props (animated-view-class))))

(def dimensions (lazy-get-react-property "Dimensions"))
(def keyboard (lazy-get-react-property "Keyboard"))
(def linking (lazy-get-react-property "Linking"))
(def desktop-notification (.-DesktopNotification (.-NativeModules js-dependencies/react-native)))

(def max-font-size-multiplier 1.25)

(defn prepare-text-props [props]
  (-> props
      (update :style typography/get-style)
      (assoc :max-font-size-multiplier max-font-size-multiplier)))

(defn prepare-nested-text-props [props]
  (-> props
      (update :style typography/get-nested-style)
      (assoc :parseBasicMarkdown true)
      (assoc :nested? true)))

;; Accessor methods for React Components
(defn text
  "For nested text elements, use nested-text instead"
  ([text-element]
   (text {} text-element))
  ([options text-element]
   [text-class (prepare-text-props options) text-element]))

(defn nested-text
  "Returns nested text elements with proper styling and typography
  Do not use the nested? option, it is for internal usage of the function only"
  [options & nested-text-elements]
  (let [options-with-style (if (:nested? options)
                             (prepare-nested-text-props options)
                             (prepare-text-props options))]
    (reduce (fn [acc text-element]
              (conj acc
                    (if (string? text-element)
                      text-element
                      (let [[options & nested-text-elements] text-element]
                        (apply nested-text (prepare-nested-text-props options)
                               nested-text-elements)))))
            [text-class (dissoc options-with-style :nested?)]
            nested-text-elements)))

(def text-input-refs (atom #{}))

(defn text-input
  [options text]
  (let [input-ref (atom nil)]
    (reagent/create-class
      {:component-will-unmount #(when @input-ref
                                  (swap! text-input-refs disj @input-ref))
       :reagent-render
       (fn [options text]
         [(text-input-class)
          (merge
            {:underline-color-android  :transparent
             :max-font-size-multiplier max-font-size-multiplier
             :placeholder-text-color   colors/text-gray
             :placeholder              (i18n/label :t/type-a-message)
             :ref                      (fn [r] 
                                         (if r
                                           (when (nil? @input-ref)
                                             (swap! text-input-refs conj r))
                                           (when @input-ref
                                             (swap! text-input-refs disj @input-ref)))
                                         (reset! input-ref r)
                                         (when (:ref options) 
                                           ((:ref options) r)))
             :value                    text}
          (-> options
              (dissoc :ref)
              (update :style typography/get-style)
              (update :style dissoc :line-height)))])})))

(defn i18n-text
  [{:keys [style key]}]
  [text {:style  style} (i18n/label key)])

(defn icon
  ([n] (icon n styles/icon-default))
  ([n style]
   [image {:source     {:uri (keyword (str "icon_" (name n)))}
           :resizeMode "contain"
           :style      style}]))

(defn touchable-opacity [props content]
  [(touchable-opacity-class) props content])

(defn touchable-highlight [props content]
  [(touchable-highlight-class)
   (merge {:underlay-color :transparent} props)
   content])

(defn touchable-without-feedback [props content]
  [(touchable-without-feedback-class)
   props
   content])

(defn get-dimensions [name]
  (js->clj (.get (dimensions) name) :keywordize-keys true))

(defn list-item [component]
  (reagent/as-element component))

(defn value->picker-item [{:keys [value label]}]
  [(picker-item-class) {:value (or value "") :label (or label value "")}])

(defn picker [{:keys [style on-change selected enabled data]}]
  (into
   [(picker-class) (merge (when style {:style style})
                          (when enabled {:enabled enabled})
                          (when on-change {:on-value-change on-change})
                          (when selected {:selected-value selected}))]
    (map value->picker-item data)))

;; Image picker

(def image-picker-class js-dependencies/image-crop-picker)

(defn show-access-error [o]
  (when (= "E_PERMISSION_MISSING" (object/get o "code"))
    (utils/show-popup (i18n/label :t/error)
                      (i18n/label :t/photos-access-error))))

(defn show-image-picker
  ([images-fn]
   (show-image-picker images-fn nil))
  ([images-fn media-type]
   (let [image-picker (.-default (image-picker-class))]
     (-> image-picker
         (.openPicker (clj->js {:multiple false :mediaType (or media-type "any")}))
         (.then images-fn)
         (.catch show-access-error)))))

(def net-info (.-NetInfo (js-dependencies/net-info)))

;; Clipboard

(def sharing
  (.-Share js-dependencies/react-native))

(defn copy-to-clipboard [text]
  (.setString (.-Clipboard js-dependencies/react-native) text))

(defn get-from-clipboard [clbk]
  (let [clipboard-contents (.getString (.-Clipboard js-dependencies/react-native))]
    (.then clipboard-contents #(clbk %))))

;; HTTP Bridge

(def http-bridge js-dependencies/http-bridge)

;; KeyboardAvoidingView

(defn keyboard-avoiding-view [props & children]
  (let [view-element (if platform/ios?
                       [(keyboard-avoiding-view-class) (merge {:behavior :padding} props)]
                       [view props])]
    (vec (concat view-element children))))

(defn scroll-view [props & children]
  (vec (conj children props (scroll-view-class))))

(views/defview with-activity-indicator
  [{:keys [timeout style enabled? preview]} comp]
  (views/letsubs
      [loading (reagent/atom true)]
    {:component-did-mount (fn []
                            (if (or (nil? timeout)
                                    (> 100 timeout))
                              (reset! loading false)
                              (utils/set-timeout #(reset! loading false)
                                                 timeout)))}
    (if (and (not enabled?) @loading)
      (or preview
          [view {:style (or style {:justify-content :center
                                   :align-items     :center})}
           [activity-indicator {:animating true}]])
      comp)))

(defn navigation-wrapper
  "Wraps component so that it will be shown only when current-screen is one of views"
  [{:keys [component views current-view hide?]
    :or   {hide? false}}]
  (let [current-view? (if (set? views)
                        (views current-view)
                        (= views current-view))

        style (if current-view?
                {:flex   1
                 :zIndex 0}
                {:opacity 0
                 :flex    0
                 :zIndex -1})

        component' (if (fn? component) [component] component)]

    (when (or (not hide?) (and hide? current-view?))
      (if hide?
        component'
        [view style (if (fn? component) [component] component)]))))

(defn with-empty-preview [comp]
  [with-activity-indicator
   {:preview [view {}]}
   comp])

;; Platform-specific View

(defmulti create-main-screen-view #(cond
                                     platform/iphone-x? :iphone-x
                                     platform/ios? :ios
                                     platform/android? :android))

(defmethod create-main-screen-view :iphone-x [current-view]
  (fn [props & children]
    (let [props             (merge props
                                   {:background-color
                                    (case current-view
                                      (:wallet-send-transaction
                                       :wallet-request-transaction
                                       :wallet-send-assets
                                       :wallet-request-assets
                                       :choose-recipient
                                       :recent-recipients
                                       :select-account
                                       :wallet-send-transaction-request
                                       :contact-code
                                       :wallet-settings-hook)
                                      colors/blue

                                      (:qr-viewer
                                       :recipient-qr-code)
                                      "#2f3031"

                                      colors/white)})
          bottom-background (when (#{:recent-recipients
                                     :select-account
                                     :wallet-send-assets
                                     :wallet-request-assets} current-view)
                              [view {:background-color colors/white
                                     :position         :absolute
                                     :bottom           0
                                     :right            0
                                     :left             0
                                     :height           100
                                     :z-index          -1000}])
          children (conj children bottom-background)]
      (apply vector (safe-area-view) props children))))

(defmethod create-main-screen-view :default [_]
  view)

(views/defview main-screen-modal-view [current-view & components]
  (views/letsubs []
    (let [main-screen-view (create-main-screen-view current-view)]
      [main-screen-view styles/flex
       [(if (= current-view :chat-modal)
          view
          keyboard-avoiding-view)
        {:flex 1 :flex-direction :column}
        (apply vector view styles/flex components)]])))
