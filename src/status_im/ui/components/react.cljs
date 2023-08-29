(ns status-im.ui.components.react
  (:require ["@react-native-community/blur" :as blur]
            ["@react-native-community/clipboard" :default Clipboard]
            ["@react-native-community/masked-view" :default MaskedView]
            ["react" :as reactjs]
            ["react-native" :as react-native :refer (Keyboard BackHandler)]
            ["react-native-fast-image" :as FastImage]
            ["react-native-image-crop-picker" :default image-picker]
            ["react-native-linear-gradient" :default LinearGradient]
            ["react-native-navigation" :refer (Navigation)]
            [quo.design-system.colors :as colors]
            [reagent.core :as reagent]
            [utils.i18n :as i18n]
            [status-im.ui.components.typography :as typography]
            [status-im.utils.platform :as platform]
            [status-im.utils.utils :as utils])
  (:require-macros [status-im.utils.views :as views]))

(def native-modules (.-NativeModules react-native))

;; React Components

(def app-state (.-AppState react-native))
(def view (reagent/adapt-react-class (.-View react-native)))

(def scroll-view-class (reagent/adapt-react-class (.-ScrollView react-native)))
(def keyboard-avoiding-view-class (reagent/adapt-react-class (.-KeyboardAvoidingView react-native)))

(def text-class (reagent/adapt-react-class (.-Text react-native)))
(def text-input-class (reagent/adapt-react-class (.-TextInput react-native)))

(def image-class (reagent/adapt-react-class (reactjs/memo (.-Image react-native))))

(def fast-image-class (reagent/adapt-react-class FastImage))

(defn image-get-size [uri callback] (.getSize (.-Image react-native) uri callback))
(defn resolve-asset-source
  [uri]
  (js->clj (.resolveAssetSource (.-Image react-native) uri) :keywordize-keys true))

(def linear-gradient (reagent/adapt-react-class LinearGradient))

(def masked-view (reagent/adapt-react-class MaskedView))

(def blur-view (reagent/adapt-react-class (.-BlurView blur)))

(defn valid-source?
  [source]
  (or (not (map? source))
      (not (contains? source :uri))
      (and (contains? source :uri)
           (:uri source))))

(defn image
  [{:keys [source] :as props}]
  (when (valid-source? source)
    [image-class props]))

(def switch-class (reagent/adapt-react-class (.-Switch react-native)))

(defn switch
  [props]
  [switch-class props])

(def touchable-highlight-class (reagent/adapt-react-class (.-TouchableHighlight react-native)))
(def pressable-class (reagent/adapt-react-class (.-Pressable react-native)))
(def touchable-without-feedback-class
  (reagent/adapt-react-class (.-TouchableWithoutFeedback react-native)))
(def touchable-opacity-class (reagent/adapt-react-class (.-TouchableOpacity react-native)))
(def activity-indicator-class (reagent/adapt-react-class (.-ActivityIndicator react-native)))

(defn activity-indicator
  [props]
  [activity-indicator-class (update props :color #(or % colors/gray))])

(defn small-loading-indicator
  [color]
  [activity-indicator
   {:color   color
    :ios     {:size :small}
    :android {:size :16}}])

(def animated (.-Animated react-native))

(def animated-view-class
  (reagent/adapt-react-class (.-View ^js animated)))

(def animated-flat-list-class
  (reagent/adapt-react-class (.-FlatList ^js animated)))

(def animated-scroll-view-class
  (reagent/adapt-react-class (.-ScrollView ^js animated)))

(defn animated-view
  [props & content]
  (vec (conj content props animated-view-class)))

(defn animated-scroll-view
  [props & children]
  (vec (conj children props animated-scroll-view-class)))

(def dimensions (.-Dimensions react-native))
(def keyboard (.-Keyboard react-native))
(def dismiss-keyboard! #(.dismiss ^js Keyboard))
(def linking (.-Linking react-native))

(def max-font-size-multiplier 1.25)

(defn prepare-text-props
  [props]
  (-> props
      (update :style typography/get-style)
      (assoc :max-font-size-multiplier max-font-size-multiplier)))

(defn prepare-nested-text-props
  [props]
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
                        (apply nested-text
                               (prepare-nested-text-props options)
                               nested-text-elements)))))
            [text-class (dissoc options-with-style :nested?)]
            nested-text-elements)))

;; We track all currently mounted text input refs
;; in a ref-to-defaultValue map
;; so that we can clear them (restore their default values)
;; when global react-navigation's onWillBlur event is invoked
(def text-input-refs (atom {}))

(defn text-input
  [options _]
  (let [render-fn (fn [options value]
                    [text-input-class
                     (merge
                      {:underline-color-android  :transparent
                       :max-font-size-multiplier max-font-size-multiplier
                       :placeholder-text-color   colors/text-gray
                       :placeholder              (i18n/label :t/type-a-message)
                       :value                    value}
                      (-> options
                          (dissoc :preserve-input?)
                          (update :style typography/get-style)
                          (update :style dissoc :line-height)))])]
    (if (:preserve-input? options)
      render-fn
      (let [input-ref (atom nil)]
        (reagent/create-class
         {:component-will-unmount #(when @input-ref
                                     (swap! text-input-refs dissoc @input-ref))
          :reagent-render
          (fn [options value]
            (render-fn (assoc options
                              :ref
                              (fn [r]
                                ;; Store input and its defaultValue
                                ;; one we receive a non-nil ref
                                (when (and r (nil? @input-ref))
                                  (swap! text-input-refs assoc r (:default-value options)))
                                (reset! input-ref r)
                                (when (:ref options)
                                  ((:ref options) r))))
                       value))})))))

(defn i18n-text
  [{style :style k :key}]
  [text {:style style} (i18n/label k)])

(defn touchable-opacity
  [props content]
  [touchable-opacity-class props content])

(defn touchable-highlight
  [props content]
  [touchable-highlight-class
   (merge {:underlay-color :transparent} props)
   content])

(defn pressable
  [props content]
  [pressable-class props content])

(defn touchable-without-feedback
  [props content]
  [touchable-without-feedback-class
   props
   content])

(defn get-dimensions
  [name]
  (js->clj (.get ^js dimensions name) :keywordize-keys true))

;; Image picker
(defn show-access-error
  [o]
  (when (= "E_PERMISSION_MISSING" (.-code ^js o))
    (utils/show-popup (i18n/label :t/error)
                      (i18n/label :t/photos-access-error))))

(defn show-image-picker
  ([images-fn]
   (show-image-picker images-fn nil))
  ([images-fn
    {:keys [media-type]
     :or   {media-type "any"}
     :as   props}]
   (-> ^js image-picker
       (.openPicker (clj->js (merge {:mediaType media-type}
                                    props)))
       (.then images-fn)
       (.catch show-access-error))))

(defn show-image-picker-camera
  ([images-fn]
   (show-image-picker-camera images-fn nil))
  ([images-fn props]
   (-> ^js image-picker
       (.openCamera (clj->js props))
       (.then images-fn)
       (.catch show-access-error))))

;; Clipboard

(def sharing
  (.-Share react-native))

(defn copy-to-clipboard
  [s]
  (.setString ^js Clipboard s))

(defn get-from-clipboard
  [clbk]
  (let [clipboard-contents (.getString ^js Clipboard)]
    (.then clipboard-contents #(clbk %))))

;; KeyboardAvoidingView
(def navigation-const (atom nil))

(.then (.constants Navigation)
       (fn [^js consts]
         (reset! navigation-const {:top-bar-height     (.-topBarHeight consts)
                                   :bottom-tabs-height (.-bottomTabsHeight consts)
                                   :status-bar-height  (.-statusBarHeight consts)})))

(defn keyboard-avoiding-view
  [props & children]
  (into [keyboard-avoiding-view-class
         (merge (when platform/ios? {:behavior :padding})
                (if (:ignore-offset props)
                  props
                  (update props :keyboardVerticalOffset + 44 (:status-bar-height @navigation-const))))]
        children))

(defn keyboard-avoiding-view-new
  [props & children]
  (into [keyboard-avoiding-view-class
         (merge (when platform/ios? {:behavior :padding})
                (if (:ignore-offset props)
                  props
                  (update props :keyboardVerticalOffset + 44)))]
        children))

(defn scroll-view
  [props & children]
  (vec (conj children props scroll-view-class)))

(views/defview with-activity-indicator
  [{:keys [timeout style enabled? preview]} component]
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
          [view
           {:style (or style
                       {:justify-content :center
                        :align-items     :center})}
           [activity-indicator {:animating true}]])
      component)))

(defn hw-back-add-listener
  [callback]
  (.addEventListener BackHandler "hardwareBackPress" callback))

(defn hw-back-remove-listener
  [callback]
  (.removeEventListener BackHandler "hardwareBackPress" callback))
