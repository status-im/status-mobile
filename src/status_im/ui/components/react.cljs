(ns status-im.ui.components.react
  (:require-macros [status-im.utils.views :as views])
  (:require [clojure.string :as string]
            [reagent.core :as r]
            [status-im.ui.components.styles :as st]
            [status-im.utils.utils :as u]
            [status-im.utils.platform :refer [platform-specific ios?]]
            [status-im.i18n :as i18n]
            [status-im.react-native.js-dependencies :as rn-dependencies]))

(defn get-react-property [name]
  (if rn-dependencies/react-native
    (aget rn-dependencies/react-native name)
    #js {}))

(defn adapt-class [class]
  (when class
    (r/adapt-react-class class)))

(defn get-class [name]
  (adapt-class (get-react-property name)))

(def native-modules (.-NativeModules rn-dependencies/react-native))
(def device-event-emitter (.-DeviceEventEmitter rn-dependencies/react-native))
(def dismiss-keyboard! rn-dependencies/dismiss-keyboard)
(def orientation rn-dependencies/orientation)
(def back-handler (get-react-property "BackHandler"))

(def splash-screen (.-SplashScreen native-modules))

;; React Components

(def app-registry (get-react-property "AppRegistry"))
(def app-state (get-react-property "AppState"))
(def net-info (get-react-property "NetInfo"))
(def view (get-class "View"))

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

(def pan-responder (.-PanResponder rn-dependencies/react-native))
(def animated (.-Animated rn-dependencies/react-native))
(def animated-view (r/adapt-react-class (.-View animated)))
(def animated-text (r/adapt-react-class (.-Text animated)))

(def dimensions (.-Dimensions rn-dependencies/react-native))
(def keyboard (.-Keyboard rn-dependencies/react-native))
(def linking (.-Linking rn-dependencies/react-native))

(def slider (get-class "Slider"))
;; Accessor methods for React Components

(defn add-font-style [style-key {:keys [font] :as opts :or {font :default}}]
  (let [font  (get-in platform-specific [:fonts (keyword font)])
        style (get opts style-key)]
    (-> opts
        (dissoc :font)
        (assoc style-key (merge style font)))))

(defn text
  ([t]
   (r/as-element [text-class t]))
  ([{:keys [uppercase?] :as opts} t & ts]
   (r/as-element
     (let [ts (cond->> (conj ts t)
                       uppercase? (map #(when % (string/upper-case %))))]
       (vec (concat
              [text-class (add-font-style :style opts)]
              ts))))))


(defn text-input [{:keys [font style] :as opts
                   :or   {font :default}} text]
  (let [font (get-in platform-specific [:fonts (keyword font)])]
    [text-input-class (merge
                       {:underline-color-android :transparent
                        :placeholder-text-color  st/text2-color
                        :placeholder             (i18n/label :t/type-a-message)
                        :value                   text}
                       (-> opts
                           (dissoc :font)
                           (assoc :style (merge style font))))]))

(defn icon
  ([n] (icon n st/icon-default))
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

(def gradient (adapt-class (.-default rn-dependencies/linear-gradient)))

(defn linear-gradient [props]
  [gradient props])

(defn list-item [component]
  (r/as-element component))

(defn picker
  ([{:keys [style item-style selected on-change]} items]
   [picker-class {:selectedValue selected :style style :itemStyle item-style :onValueChange on-change}
    (for [{:keys [label value]} items]
      ^{:key (str value)}
      [picker-item-class
       {:label (or label value) :value value}])]))

;; Image picker

(def image-picker-class rn-dependencies/image-crop-picker)

(defn show-access-error [o]
  (when (= "ERROR_PICKER_UNAUTHORIZED_KEY" (aget o "code")) ; Do not show error when user cancel selection
    (u/show-popup (i18n/label :t/error)
                  (i18n/label :t/photos-access-error))))

(defn show-image-picker [images-fn]
  (let [image-picker (.-default image-picker-class)]
    (-> image-picker
        (.openPicker (clj->js {:multiple false}))
        (.then images-fn)
        (.catch show-access-error))))

;; Clipboard

(def sharing
  (.-Share rn-dependencies/react-native))

(defn copy-to-clipboard [text]
  (.setString (.-Clipboard rn-dependencies/react-native) text))

(defn get-from-clipboard [clbk]
  (let [clipboard-contents (.getString (.-Clipboard rn-dependencies/react-native))]
    (.then clipboard-contents #(clbk %))))


;; Emoji

(def emoji-picker-class rn-dependencies/emoji-picker)

(def emoji-picker
  (let [emoji-picker (.-default emoji-picker-class)]
    (r/adapt-react-class emoji-picker)))

;; Autolink

(def autolink-class (r/adapt-react-class (.-default rn-dependencies/autolink)))

(defn autolink [opts]
  (r/as-element
   [autolink-class (add-font-style :style opts)]))

;; HTTP Bridge

(def http-bridge rn-dependencies/http-bridge)

;; KeyboardAvoidingView

(defn keyboard-avoiding-view [props & children]
  (let [view-element (if ios?
                       [keyboard-avoiding-view-class (merge {:behavior :padding} props)]
                       [view props])]
    (vec (concat view-element children))))

(views/defview with-activity-indicator
  [{:keys [timeout style enabled? preview]} comp]
  (views/letsubs
    [loading (r/atom true)]
    {:component-did-mount (fn []
                            (if (or (nil? timeout)
                                    (> 100 timeout))
                              (reset! loading false)
                              (js/setTimeout (fn []
                                               (reset! loading false))
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

        style         (if current-view?
                        {:flex 1}
                        {:opacity 0
                         :flex    0})

        component' (if (fn? component) [component] component)]

    (when (or (not hide?) (and hide? current-view?))
      (if hide?
        component'
        [view style (if (fn? component) [component] component)]))))

(defn with-empty-preview [comp]
  [with-activity-indicator
   {:preview [view {}]}
   comp])
