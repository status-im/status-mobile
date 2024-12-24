(ns legacy.status-im.ui.components.react
  (:require
    ["@react-native-clipboard/clipboard" :default Clipboard]
    ["@react-native-community/blur" :as blur]
    ["react" :as reactjs]
    ["react-native" :as react-native :refer (Keyboard)]
    ["react-native-fast-image" :as FastImage]
    ["react-native-navigation" :refer (Navigation)]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.text-style :as typography]
    [react-native.platform :as platform]
    [reagent.core :as reagent]
    [utils.i18n :as i18n]))


;; React Components

(def view (reagent/adapt-react-class (.-View react-native)))

(def scroll-view-class (reagent/adapt-react-class (.-ScrollView react-native)))
(def keyboard-avoiding-view-class (reagent/adapt-react-class (.-KeyboardAvoidingView react-native)))

(def text-class (reagent/adapt-react-class (.-Text react-native)))
(def text-input-class (reagent/adapt-react-class (.-TextInput react-native)))

(def image-class (reagent/adapt-react-class (reactjs/memo (.-Image react-native))))

(def fast-image-class (reagent/adapt-react-class FastImage))

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
(def activity-indicator-class (reagent/adapt-react-class (.-ActivityIndicator react-native)))

(defn activity-indicator
  [props]
  [activity-indicator-class (update props :color #(or % colors/gray))])

(def animated (.-Animated react-native))

(def animated-view-class
  (reagent/adapt-react-class (.-View ^js animated)))

(def animated-flat-list-class
  (reagent/adapt-react-class (.-FlatList ^js animated)))

(defn animated-view
  [props & content]
  (vec (conj content props animated-view-class)))

(def dimensions (.-Dimensions react-native))
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

(defn touchable-highlight
  [props content]
  [touchable-highlight-class
   (merge {:underlay-color :transparent} props)
   content])

(defn get-dimensions
  [name]
  (js->clj (.get ^js dimensions name) :keywordize-keys true))


;; Clipboard

(def sharing
  (.-Share react-native))

(defn copy-to-clipboard
  [s]
  (.setString ^js Clipboard s))



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
                  (update props :keyboard-vertical-offset + 44 (:status-bar-height @navigation-const))))]
        children))

(defn scroll-view
  [props & children]
  (vec (conj children props scroll-view-class)))
