(ns quo.react-native
  (:require ["@react-native-community/hooks" :as hooks]
            ["react-native" :as rn]
            ["react-native-draggable-flatlist" :default DraggableFlatList]
            ["react-native-hole-view" :refer (RNHoleView)]
            ["react-native-navigation" :refer (Navigation)]
            ["rn-emoji-keyboard" :refer (EmojiKeyboard)]
            [cljs-bean.core :as bean]
            [quo.platform :as platform]
            [reagent.core :as reagent]))

(def hole-view (reagent/adapt-react-class RNHoleView))

(def rn-draggable-flatlist (reagent/adapt-react-class DraggableFlatList))

(def emoji-keyboard (reagent/adapt-react-class EmojiKeyboard))

(def app-registry (.-AppRegistry rn))

(def platform (.-Platform ^js rn))

(def find-node-handle (.-findNodeHandle ^js rn))

(def view (reagent/adapt-react-class (.-View ^js rn)))
(def image (reagent/adapt-react-class (.-Image rn)))
(def text (reagent/adapt-react-class (.-Text ^js rn)))

(defn resolve-asset-source
  [uri]
  (js->clj (.resolveAssetSource ^js (.-Image ^js rn) uri) :keywordize-keys true))

(def scroll-view (reagent/adapt-react-class (.-ScrollView ^js rn)))
(def modal (reagent/adapt-react-class (.-Modal ^js rn)))
(def refresh-control (reagent/adapt-react-class (.-RefreshControl ^js rn)))

(def touchable-opacity (reagent/adapt-react-class (.-TouchableOpacity ^js rn)))
(def touchable-highlight (reagent/adapt-react-class (.-TouchableHighlight ^js rn)))
(def touchable-without-feedback (reagent/adapt-react-class (.-TouchableWithoutFeedback ^js rn)))
(def text-input (reagent/adapt-react-class (.-TextInput ^js rn)))

(def keyboard-avoiding-view-class (reagent/adapt-react-class (.-KeyboardAvoidingView ^js rn)))

(def navigation-const (atom nil))

(.then (.constants Navigation)
       (fn [^js consts]
         (reset! navigation-const {:top-bar-height     (.-topBarHeight consts)
                                   :bottom-tabs-height (.-bottomTabsHeight consts)
                                   :status-bar-height  (.-statusBarHeight consts)})))

(defn keyboard-avoiding-view
  []
  (let [this  (reagent/current-component)
        props (reagent/props this)]
    (into [keyboard-avoiding-view-class
           (merge (when platform/ios?
                    {:behavior :padding})
                  props
                  {:keyboardVerticalOffset (+ 44 (:status-bar-height @navigation-const))})]
          (reagent/children this))))

(def status-bar (.-StatusBar ^js rn))

(def keyboard (.-Keyboard ^js rn))

(def dismiss-keyboard! #(.dismiss ^js keyboard))

(def dimensions (.-Dimensions ^js rn))

(def pan-responder (.-PanResponder ^js rn))

(defn create-pan-responder
  [opts]
  (.create ^js pan-responder (clj->js opts)))

(def animated (.-Animated rn))

(def subtract (.-subtract ^js animated))

(def animated-flat-list-class
  (reagent/adapt-react-class (.-FlatList ^js animated)))

(def animated-view
  (reagent/adapt-react-class (.-View ^js animated)))

(def ui-manager (.-UIManager ^js rn))

(def layout-animation (.-LayoutAnimation ^js rn))
(def configure-next (.-configureNext ^js layout-animation))
(def create-animation (.-create ^js layout-animation))

(def layout-animation-presets
  {:ease-in-ease-out (-> ^js layout-animation .-Presets .-easeInEaseOut)
   :linear           (-> ^js layout-animation .-Presets .-linear)
   :spring           (-> ^js layout-animation .-Presets .-spring)})

(def layout-animation-types
  {:spring           (-> ^js layout-animation .-Types .-spring)
   :linear           (-> ^js layout-animation .-Types .-linear)
   :ease-in-ease-out (-> ^js layout-animation .-Types .-easeInEaseOut)
   :ease-in          (-> ^js layout-animation .-Types .-easeIn)
   :ease-out         (-> ^js layout-animation .-Types .-easeOut)})

(def layout-animation-properties
  {:opacity  (-> ^js layout-animation .-Properties .-opacity)
   :scale-x  (-> ^js layout-animation .-Properties .-scaleX)
   :scale-y  (-> ^js layout-animation .-Properties .-scaleY)
   :scale-xy (-> ^js layout-animation .-Properties .-scaleXY)})

(def custom-animations
  {:ease-opacity-200 #js
                      {:duration 200
                       :create   #js
                                  {:type     (:ease-in-ease-out layout-animation-types)
                                   :property (:opacity layout-animation-properties)}
                       :update   #js
                                  {:type     (:ease-in-ease-out layout-animation-types)
                                   :property (:opacity layout-animation-properties)}
                       :delete   #js
                                  {:type     (:ease-in-ease-out layout-animation-types)
                                   :property (:opacity layout-animation-properties)}}})

(defonce enable-layout-animations
  (when platform/android?
    (.setLayoutAnimationEnabledExperimental ^js ui-manager true)))

(def activity-indicator (reagent/adapt-react-class (.-ActivityIndicator ^js rn)))

(def pressable (reagent/adapt-react-class (.-Pressable ^js rn)))

;; Flat-list
(def ^:private rn-flat-list (reagent/adapt-react-class (.-FlatList ^js rn)))

(defn- wrap-render-fn
  [f render-data]
  (fn [data]
    (reagent/as-element [f (.-item ^js data) (.-index ^js data)
                         (.-separators ^js data) render-data
                         (.-isActive ^js data) (.-drag ^js data)])))

(defn- wrap-on-drag-end-fn
  [f]
  (fn [data]
    (f (.-from ^js data) (.-to ^js data) (.-data ^js data))))

(defn- wrap-key-fn
  [f]
  (fn [data index]
    {:post [(some? %)]}
    (f data index)))

(defn base-list-props
  [{:keys [key-fn render-fn empty-component header footer separator data render-data on-drag-end-fn]
    :as   props}]
  (merge
   {:data (to-array data)}
   (when key-fn {:keyExtractor (wrap-key-fn key-fn)})
   (when render-fn {:renderItem (wrap-render-fn render-fn render-data)})
   (when separator {:ItemSeparatorComponent (fn [] (reagent/as-element separator))})
   (when empty-component {:ListEmptyComponent (fn [] (reagent/as-element empty-component))})
   (when header {:ListHeaderComponent (reagent/as-element header)})
   (when footer {:ListFooterComponent (reagent/as-element footer)})
   (when on-drag-end-fn {:onDragEnd (wrap-on-drag-end-fn on-drag-end-fn)})
   (dissoc props :data :header :footer :empty-component :separator :render-fn :key-fn :on-drag-end-fn)))

(defn flat-list
  [props]
  [rn-flat-list (base-list-props props)])

(defn draggable-flat-list
  [props]
  [rn-draggable-flatlist (base-list-props props)])

(defn animated-flat-list
  [props]
  [animated-flat-list-class (base-list-props props)])
;; Hooks

(defn use-window-dimensions
  []
  (let [window (rn/useWindowDimensions)]
    {:font-scale (.-fontScale window)
     :height     (.-height ^js window)
     :scale      (.-scale ^js window)
     :width      (.-width ^js window)}))

(def use-back-handler (.-useBackHandler hooks))

(defn use-keyboard
  []
  (let [kb (.useKeyboard hooks)]
    {:keyboard-shown  (.-keyboardShown ^js kb)
     :keyboard-height (.-keyboardHeight ^js kb)}))

(defn use-layout
  []
  (let [{:keys [onLayout x y height width]} (bean/bean (.useLayout hooks))]
    {:on-layout onLayout
     :x         x
     :y         y
     :height    height
     :width     width}))
