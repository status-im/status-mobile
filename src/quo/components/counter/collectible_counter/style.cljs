(ns quo.components.counter.collectible-counter.style
  (:require
    [quo.foundations.colors :as colors]))

(defn- get-background-color
  [{:keys [status]} theme]
  (case status
    :default (colors/theme-colors colors/white-opa-70 colors/neutral-95-opa-70 theme)
    :error   (colors/resolve-color :danger theme 10)
    (colors/theme-colors colors/white-opa-70 colors/neutral-95-opa-70 theme)))

(defn- get-container-border-styles
  [{:keys [status]} theme]
  (when (= status :error)
    {:border-color (colors/resolve-color :danger theme 20)
     :border-width 1}))

(defn- get-container-styles-by-size
  [{:keys [size]}]
  (let [style-size-24 {:height             24
                       :padding-vertical   3
                       :padding-horizontal 8}
        style-size-32 {:height             32
                       :padding-vertical   5
                       :padding-horizontal 12}]
    (case size
      :size-32 style-size-32
      :size-24 style-size-24
      style-size-32)))

(defn container
  [props theme]
  (merge {:align-self       :flex-start
          :flex-direcrion   :row
          :justify-content  :center
          :border-radius    999
          :background-color (get-background-color props theme)}
         (get-container-border-styles props theme)
         (get-container-styles-by-size props)))

(defn- get-text-color
  [{:keys [status]} theme]
  (case status
    :default (colors/theme-colors colors/neutral-100 colors/white theme)
    :error   (colors/resolve-color :danger theme)
    (colors/theme-colors colors/neutral-100 colors/white theme)))

(defn get-text-size
  [{:keys [size]}]
  (case size
    :size-32 :paragraph-1
    :size-24 :paragraph-2
    :paragraph-1))

(defn text
  [props theme]
  {:color (get-text-color props theme)})
