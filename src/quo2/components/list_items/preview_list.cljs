(ns quo2.components.list-items.preview-list
  (:require [quo2.components.avatars.user-avatar.view :as user-avatar]
            [quo2.components.icon :as quo2.icons]
            [quo2.components.markdown.text :as quo2.text]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [react-native.hole-view :as hole-view]
            [quo2.theme :as quo.theme]))

(def params
  {32 {:border-radius {:circular 16 :rounded 10}
       :hole-radius   {:circular 18 :rounded 12}
       :margin-left   -8
       :hole-size     36
       :hole-x        22
       :hole-y        -2}
   24 {:border-radius {:circular 12 :rounded 8}
       :hole-radius   {:circular 13 :rounded 9}
       :margin-left   -4
       :hole-size     26
       :hole-x        19
       :hole-y        -1}
   20 {:border-radius {:circular 10 :rounded 8}
       :hole-radius   {:circular 11 :rounded 9}
       :margin-left   -4
       :hole-size     22
       :hole-x        15
       :hole-y        -1}
   16 {:border-radius {:circular 8 :rounded 8}
       :hole-radius   {:circular 9 :rounded 9}
       :margin-left   -4
       :hole-size     18
       :hole-x        11
       :hole-y        -1}})

;; TODO - Add avatar components for other types once implemented
(defn avatar
  [item type size border-radius]
  (case type
    :user                          [user-avatar/user-avatar
                                    (merge {:ring?             false
                                            :status-indicator? false
                                            :size              (case size
                                                                 32 :small
                                                                 24 :xs
                                                                 16 :xxxs)}
                                           item)]
    (:photo :collectible :network) [fast-image/fast-image
                                    {:source (:source item)
                                     :style  {:width         size
                                              :height        size
                                              :border-radius border-radius}}]))

(defn list-item
  [index type size item list-size margin-left
   hole-size hole-radius hole-x hole-y border-radius]
  (let [last-item? (= index (- list-size 1))]
    [hole-view/hole-view
     {:style {:margin-left (if (= index 0) 0 margin-left)}
      :holes (if last-item?
               []
               [{:x            hole-x
                 :y            hole-y
                 :width        hole-size
                 :height       hole-size
                 :borderRadius hole-radius}])}
     [avatar item type size border-radius]]))

(defn get-overflow-color
  [transparent? transparent-color light-color dark-color theme]
  (if transparent?
    transparent-color
    (colors/theme-colors light-color dark-color theme)))

(defn overflow-label
  [{:keys [label size transparent? border-radius margin-left theme more-than-99-label]}]
  [rn/view
   {:style {:width            size
            :height           size
            :margin-left      margin-left
            :border-radius    border-radius
            :justify-content  :center
            :align-items      :center
            :background-color (get-overflow-color
                               transparent?
                               colors/white-opa-10
                               colors/neutral-20
                               colors/neutral-70
                               theme)}}
   (if (= size 16)
     [quo2.icons/icon :i/more
      {:size  12
       :color (get-overflow-color
               transparent?
               colors/white-opa-70
               colors/neutral-50
               colors/neutral-40
               theme)}]
     [quo2.text/text
      {:size   (if (= size 32) :paragraph-2 :label)
       :weight :medium
       :style  {:color       (get-overflow-color
                              transparent?
                              colors/white-opa-70
                              colors/neutral-60
                              colors/neutral-40
                              theme)
                :margin-left -2}}
      ;; If overflow label is below 100, show label as +label (ex. +30), else just show 99+
      (if (< label 100)
        (str "+" label)
        more-than-99-label)])])

(defn border-type
  [type]
  (case type
    (:account :collectible :photo) :rounded
    :circular))

(defn- preview-list-internal
  "[preview-list opts items]
   opts
   {:type          :user/:community/:account/:token/:collectible/:dapp/:network
    :size          32/24/16
    :list-size     override items count in overflow label (optional)
    :transparent?  overflow-label transparent?}
   items           preview list items (only 4 items is required for preview)
  "
  [{:keys [type size list-size transparent? theme more-than-99-label]} items]
  (let [items-arr     (into [] items)
        list-size     (or list-size (count items))
        margin-left   (get-in params [size :margin-left])
        hole-size     (get-in params [size :hole-size])
        border-radius (get-in params [size :border-radius (border-type type)])
        hole-radius   (get-in params [size :hole-radius (border-type type)])
        hole-x        (get-in params [size :hole-x])
        hole-y        (get-in params [size :hole-y])]
    [rn/view {:style {:flex-direction :row}}
     (for [index (range (if (> list-size 4) 3 list-size))]
       ^{:key (str index list-size)}
       [list-item index type size (get items-arr index) list-size
        margin-left hole-size hole-radius hole-x hole-y border-radius])
     (when (> list-size 4)
       [overflow-label
        {:label              (- list-size 3)
         :size               size
         :transparent?       transparent?
         :border-radius      border-radius
         :margin-left        margin-left
         :theme              theme
         :more-than-99-label more-than-99-label}])]))

(def preview-list (quo.theme/with-theme preview-list-internal))
