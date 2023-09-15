(ns react-native.flat-list
  (:require ["react-native" :as react-native]
            [reagent.core :as reagent]))

(def react-native-flat-list (reagent/adapt-react-class (.-FlatList ^js react-native)))

(defn- wrap-render-fn
  [f render-data]
  (fn [^js data]
    (reagent/as-element [f (.-item data) (.-index data)
                         (.-separators data) render-data
                         (.-isActive data) (.-drag data)])))

(defn- wrap-on-drag-end-fn
  [f]
  (fn [^js data]
    (f (.-from data) (.-to data) (.-data data))))

(defn- wrap-key-fn
  [f]
  (fn [data index]
    (when f
      (f data index))))

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
