(ns quo2.components.list-items.preview-list.view
  (:require [quo2.components.avatars.account-avatar.view :as account-avatar]
            [quo2.components.avatars.user-avatar.view :as user-avatar]
            [quo2.components.icon :as quo2.icons]
            [quo2.components.markdown.text :as quo2.text]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as quo.theme]
            [quo2.components.list-items.preview-list.properties :as properties]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [react-native.hole-view :as hole-view]))

;; This overflow item needs to be cleaned up once the "number tag" component is implemented
;; https://github.com/status-im/status-mobile/issues/17045
(defn get-overflow-color
  [blur? blur-light-color blur-dark-color light-color dark-color theme]
  (if blur?
    (colors/theme-colors blur-light-color blur-dark-color theme)
    (colors/theme-colors light-color dark-color theme)))

(def more-icon-for-sizes #{16 14})

(defn overflow-label
  [{:keys [label size blur? border-radius margin-left theme more-than-99-label]}]
  [rn/view
   {:style {:width            size
            :height           size
            :margin-left      margin-left
            :border-radius    border-radius
            :justify-content  :center
            :align-items      :center
            :background-color (get-overflow-color
                               blur?
                               colors/neutral-80-opa-5
                               colors/white-opa-5
                               colors/neutral-20
                               colors/neutral-90
                               theme)}}
   (if (more-icon-for-sizes size)
     [quo2.icons/icon :i/more
      {:size  12
       :color (get-overflow-color
               blur?
               colors/neutral-80-opa-70
               colors/white-opa-70
               colors/neutral-50
               colors/neutral-40
               theme)}]
     [quo2.text/text
      {:size   (if (= size 32) :paragraph-2 :label)
       :weight :medium
       :style  {:color       (get-overflow-color
                              blur?
                              colors/neutral-80-opa-70
                              colors/white-opa-70
                              colors/neutral-50
                              colors/neutral-40
                              theme)
                :margin-left -2}}
      ;; If overflow label is below 100, show label as +label (ex. +30), else just show 99+
      (if (< label 100)
        (str "+" label)
        more-than-99-label)])])

(defn- preview-item
  [{:keys [item type size-key]}]
  (let [size             (get-in properties/sizes [size-key :size])
        user-avatar-size (get-in properties/sizes [size-key :user-avatar-size])
        border-radius    (get-in properties/sizes
                                 [size-key :border-radius (properties/border-type type)])]
    (case type
      :user                        [user-avatar/user-avatar
                                    (merge {:ring?             false
                                            :status-indicator? false
                                            :size              user-avatar-size}
                                           item)]

      :accounts                    [account-avatar/view
                                    (merge item {:size size})]

      (:communities :collectibles) [fast-image/fast-image
                                    {:source (:source item)
                                     :style  {:width         size
                                              :height        size
                                              :border-radius border-radius}}]

      (:tokens :network :dapps)    [fast-image/fast-image
                                    {:source (:source item)
                                     :style  {:width         size
                                              :height        size
                                              :border-radius border-radius}}]
      nil)))

(defn- list-item
  [{:keys [index type size-key item number]}]
  (let [last-item?                               (= index (dec number))
        border-type                              (properties/border-type type)
        {margin-left               :margin-left
         hole-size                 :hole-size
         hole-x                    :hole-x
         hole-y                    :hole-y
         {hole-radius border-type} :hole-radius} (properties/sizes size-key)]
    [hole-view/hole-view
     {:style {:margin-left (if (= index 0) 0 margin-left)}
      :holes (if last-item?
               []
               [{:x            hole-x
                 :y            hole-y
                 :width        hole-size
                 :height       hole-size
                 :borderRadius hole-radius}])}
     [preview-item
      {:item     item
       :type     type
       :size-key size-key}]]))

(defn- view-internal
  "[preview-list opts items]
   opts
   {:type          :user/:communities/:accounts/:tokens/:collectibles/:dapps/:network
    :size          :size-32 | :size-24 | :size-20 | :size-16 | :size-14
    :number        number of items in the list (optional)
    :blur?         overflow-label blur?}
   items           preview list items (only 4 items is required for preview)
  "
  [{:keys [type size number blur? theme more-than-99-label]} items]
  (let [size-key      (if (contains? properties/sizes size) size :size-24)
        number        (or number (count items))
        margin-left   (get-in properties/sizes [size-key :margin-left])
        border-radius (get-in properties/sizes [size-key :border-radius (properties/border-type type)])]
    [rn/view {:style {:flex-direction :row}}
     (for [index (range (if (> number 4) 3 number))]
       ^{:key (str index number)}
       [list-item
        {:index    index
         :type     type
         :size-key size-key
         :item     (get (vec items) index)
         :number   number}])
     (when (> number 4)
       [overflow-label
        {:label              (- number 3)
         :size               (get-in properties/sizes [size-key :size])
         :blur?              blur?
         :border-radius      border-radius
         :margin-left        margin-left
         :theme              theme
         :more-than-99-label more-than-99-label}])]))

(def view (quo.theme/with-theme view-internal))
