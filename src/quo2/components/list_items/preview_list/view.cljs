(ns quo2.components.list-items.preview-list.view
  (:require [quo2.components.avatars.account-avatar.view :as account-avatar]
            [quo2.components.avatars.user-avatar.view :as user-avatar]
            [quo2.components.tags.number-tag.view :as number-tag]
            [quo2.theme :as quo.theme]
            [quo2.components.list-items.preview-list.properties :as properties]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [react-native.hole-view :as hole-view]))

(defn- preview-item
  [{:keys [item type size-key]}]
  (let [size             (get-in properties/sizes [size-key :size])
        user-avatar-size (get-in properties/sizes [size-key :user-avatar-size])
        border-radius    (get-in properties/sizes
                                 [size-key :border-radius (properties/border-type type)])]
    (case type
      :user                        [user-avatar/user-avatar
                                    (assoc item
                                           :ring?             false
                                           :status-indicator? false
                                           :size              user-avatar-size)]

      :accounts                    [account-avatar/view
                                    (assoc item :size size)]

      (:communities :collectibles) [fast-image/fast-image
                                    {:source (or (:source item) item)
                                     :style  {:width         size
                                              :height        size
                                              :border-radius border-radius}}]

      (:tokens :network :dapps)    [fast-image/fast-image
                                    {:source (or (:source item) item)
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
  [{:keys [type size number blur?]} items]
  (let [size-key    (if (contains? properties/sizes size) size :size-24)
        number      (or number (count items))
        border-type (properties/border-type type)
        margin-left (get-in properties/sizes [size-key :margin-left])]
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
       [number-tag/view
        {:type            border-type
         :number          (str (- number 3))
         :size            size-key
         :blur?           blur?
         :container-style {:margin-left margin-left}}])]))

(def view (quo.theme/with-theme view-internal))
