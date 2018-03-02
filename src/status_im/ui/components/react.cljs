(ns status-im.ui.components.react
  (:require-macros [status-im.utils.views :as views])
  (:require [clojure.string :as string]
            [goog.object :as object]
            [reagent.core :as reagent]
            [status-im.ui.components.styles :as styles]
            [status-im.utils.utils :as utils]
            [status-im.utils.platform :as platform]
            [status-im.i18n :as i18n]
            [status-im.react-native.js-dependencies :as js-dependencies]))

(defn get-react-property [name]
  (if js-dependencies/react-native
    (object/get js-dependencies/react-native name)
    #js {}))

(defn adapt-class [class]
  (when class
    (reagent/adapt-react-class class)))

(defn get-class [name]
  (adapt-class (get-react-property name)))

(def native-modules (.-NativeModules js-dependencies/react-native))
(def device-event-emitter (.-DeviceEventEmitter js-dependencies/react-native))
(def dismiss-keyboard! js-dependencies/dismiss-keyboard)
(def orientation js-dependencies/orientation)
(def back-handler (get-react-property "BackHandler"))

(def splash-screen (.-SplashScreen native-modules))

;; React Components

(def app-registry (get-react-property "AppRegistry"))
(def app-state (get-react-property "AppState"))
(def net-info (get-react-property "NetInfo"))
(def view (get-class "View"))
(def safe-area-view (get-class "SafeAreaView"))

(def status-bar (get-class "StatusBar"))

(def scroll-view (get-class "ScrollView"))
(def web-view (get-class "WebView"))
(def keyboard-avoiding-view-class (get-class "KeyboardAvoidingView"))

(def text-class (get-class "Text"))
(def text-input-class (get-class "TextInput"))
(def image (get-class "Image"))

(def touchable-without-feedback (get-class "TouchableWithoutFeedback"))
(def touchable-highlight-class (get-class "TouchableHighlight"))
(def touchable-opacity (get-class "TouchableOpacity"))
(def activity-indicator (get-class "ActivityIndicator"))

(def modal (get-class "Modal"))
(def picker-class (get-class "Picker"))
(def picker-item-class
  (when-let [picker (get-react-property "Picker")]
    (adapt-class (.-Item picker))))

(def pan-responder (.-PanResponder js-dependencies/react-native))
(def animated (.-Animated js-dependencies/react-native))
(def animated-view (reagent/adapt-react-class (.-View animated)))
(def animated-text (reagent/adapt-react-class (.-Text animated)))

(def dimensions (.-Dimensions js-dependencies/react-native))
(def keyboard (.-Keyboard js-dependencies/react-native))
(def linking (.-Linking js-dependencies/react-native))

(def slider (get-class "Slider"))
;; Accessor methods for React Components

(defn add-font-style [style-key {:keys [font] :as opts :or {font :default}}]
  (let [font  (get-in platform/platform-specific [:fonts (keyword font)])
        style (get opts style-key)]
    (-> opts
        (dissoc :font)
        (assoc style-key (merge style font)))))

(defn text
  ([t]
   (reagent/as-element [text-class t]))
  ([{:keys [uppercase?] :as opts} t & ts]
   (reagent/as-element
     (let [ts (cond->> (conj ts t)
                       uppercase? (map #(when % (string/upper-case %))))]
       (vec (concat
              [text-class (add-font-style :style opts)]
              ts))))))


(defn text-input [{:keys [font style] :as opts
                   :or   {font :default}} text]
  (let [font (get-in platform/platform-specific [:fonts (keyword font)])]
    [text-input-class (merge
                       {:underline-color-android :transparent
                        :placeholder-text-color  styles/text2-color
                        :placeholder             (i18n/label :t/type-a-message)
                        :value                   text}
                       (-> opts
                           (dissoc :font)
                           (assoc :style (merge style font))))]))

(defn icon
  ([n] (icon n styles/icon-default))
  ([n style]
   [image {:source     {:uri (keyword (str "icon_" (name n)))}
           :resizeMode "contain"
           :style      style}]))

(defn touchable-highlight [props content]
  [touchable-highlight-class
   (merge {:underlay-color :transparent} props)
   content])

(defn get-dimensions [name]
  (js->clj (.get dimensions name) :keywordize-keys true))

(def gradient (adapt-class (.-default js-dependencies/linear-gradient)))

(defn linear-gradient [props]
  [gradient props])

(defn list-item [component]
  (reagent/as-element component))

(defn picker
  ([{:keys [style item-style selected on-change]} items]
   [picker-class {:selectedValue selected :style style :itemStyle item-style :onValueChange on-change}
    (for [{:keys [label value]} items]
      ^{:key (str value)}
      [picker-item-class
       {:label (or label value) :value value}])]))

;; Image picker

(def image-picker-class js-dependencies/image-crop-picker)

(defn show-access-error [o]
  (when (= "ERROR_PICKER_UNAUTHORIZED_KEY" (object/get o "code")) ; Do not show error when user cancel selection
    (utils/show-popup (i18n/label :t/error)
                      (i18n/label :t/photos-access-error))))

(defn show-image-picker [images-fn]
  (let [image-picker (.-default image-picker-class)]
    (-> image-picker
        (.openPicker (clj->js {:multiple false}))
        (.then images-fn)
        (.catch show-access-error))))

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
                       [keyboard-avoiding-view-class (merge {:behavior :padding} props)]
                       [view props])]
    (vec (concat view-element children))))

(defn navigation-wrapper
  "Wraps component so that it will be shown only when current-screen is one of views"
  [{:keys [component views current-view hide?]
    :or   {hide? false}}]
  (let [current-view? (if (set? views)
                        (views current-view)
                        (= views current-view))

        style         (if current-view?
                        {:flex 1}
                        {:opacity 0
                         :flex    0})

        component' (if (fn? component) [component] component)]

    (when (or (not hide?) (and hide? current-view?))
      (if hide?
        component'
        [view style (if (fn? component) [component] component)]))))

;; Platform-specific View

(def platform-specific-view
  (if platform/iphone-x? safe-area-view view))
