(ns react-native.flat-list
  (:require ["react-native" :as react-native]
            ["react-native-reanimated" :default reanimated]
            [reagent.core :as reagent]))

(def react-native-flat-list (reagent/adapt-react-class (.-FlatList ^js react-native)))
(def react-native-flat-list-animated (reagent/adapt-react-class (.-FlatList ^js reanimated)))

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
  [react-native-flat-list (base-list-props props)])

(defn flat-list-animated
  [props]
  [react-native-flat-list-animated (base-list-props props)])
