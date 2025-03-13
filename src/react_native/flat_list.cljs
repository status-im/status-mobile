(ns react-native.flat-list
  (:require
    ["react-native" :as react-native]
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

(defn dissoc-custom-props
  [props]
  (dissoc props :data :header :footer :empty-component :separator :render-fn :key-fn :on-drag-end-fn))

(defn base-list-props
  [{:keys [key-fn data render-fn empty-component header footer separator render-data on-drag-end-fn]
    :as   props}]
  (cond-> {:data (to-array data)}
    key-fn          (assoc :keyExtractor (wrap-key-fn key-fn))
    render-fn       (assoc :renderItem (wrap-render-fn render-fn render-data))
    separator       (assoc :ItemSeparatorComponent (fn [] (reagent/as-element separator)))
    empty-component (assoc :ListEmptyComponent (fn [] (reagent/as-element empty-component)))
    header          (assoc :ListHeaderComponent (reagent/as-element header))
    footer          (assoc :ListFooterComponent (reagent/as-element footer))
    on-drag-end-fn  (assoc :onDragEnd (wrap-on-drag-end-fn on-drag-end-fn))
    :always         (merge (dissoc-custom-props props))))

(defn flat-list
  [props]
  [react-native-flat-list (base-list-props props)])
