(ns quo2.components.list-items.preview-list
  (:require [quo2.components.avatars.account-avatar.view :as account-avatar]
            [quo2.components.avatars.user-avatar.view :as user-avatar]
            [quo2.components.icon :as quo2.icons]
            [quo2.components.markdown.text :as quo2.text]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as quo.theme]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [react-native.hole-view :as hole-view]))

(def params
  {32 {:border-radius {:rounded 16 :squared 10}
       :hole-radius   {:rounded 18 :squared 12}
       :margin-left   -8
       :hole-size     36
       :hole-x        22
       :hole-y        -2}
   24 {:border-radius {:rounded 12 :squared 8}
       :hole-radius   {:rounded 13 :squared 9}
       :margin-left   -4
       :hole-size     26
       :hole-x        19
       :hole-y        -1}
   20 {:border-radius {:rounded 10 :squared 8}
       :hole-radius   {:rounded 11 :squared 9}
       :margin-left   -4
       :hole-size     22
       :hole-x        15
       :hole-y        -1}
   16 {:border-radius {:rounded 8 :squared 8}
       :hole-radius   {:rounded 9 :squared 9}
       :margin-left   -4
       :hole-size     18
       :hole-x        11
       :hole-y        -1}
   14 {:border-radius {:rounded 7 :squared 7}
       :hole-radius   {:rounded 8 :squared 8}
       :margin-left   -2
       :hole-size     16
       :hole-x        11
       :hole-y        -1}})

(def more-icon-for-sizes #{16 14})

(defn avatar
  [item type size border-radius]
  (case type
    :user                                                [user-avatar/user-avatar
                                                          (merge {:ring?             false
                                                                  :status-indicator? false
                                                                  :size              (case size
                                                                                       32 :small
                                                                                       24 :xs
                                                                                       20 :xxs
                                                                                       16 :xxxs)}
                                                                 item)]

    :accounts                                            [account-avatar/view
                                                          (merge item {:size size})]

    (:communities :tokens :collectibles :network :dapps) [fast-image/fast-image
                                                          {:source (:source item)
                                                           :style  {:width         size
                                                                    :height        size
                                                                    :border-radius border-radius}}]
    nil))

(defn list-item
  [index type size item number margin-left
   hole-size hole-radius hole-x hole-y border-radius]
  (let [last-item? (= index (- number 1))]
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

;; This needs to be cleaned up once the "number tag" component is implemented
;; https://github.com/status-im/status-mobile/issues/17045
(defn get-overflow-color
  [blur? blur-light-color blur-dark-color light-color dark-color theme]
  (if blur?
    (colors/theme-colors blur-light-color blur-dark-color theme)
    (colors/theme-colors light-color dark-color theme)))

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
   (if (some more-icon-for-sizes [size])
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

(defn border-type
  [type]
  (case type
    (:accounts :collectibles) :squared
    :rounded))

(defn- view-internal
  "[preview-list opts items]
   opts
   {:type          :user/:communities/:accounts/:tokens/:collectibles/:dapps/:network
    :size          32/24/20/16/14
    :number        number of items in the list (optional)
    :blur?         overflow-label blur?}
   items           preview list items (only 4 items is required for preview)
  "
  [{:keys [type size number blur? theme more-than-99-label]} items]
  (let [items-arr     (into [] items)
        number        (or number (count items))
        margin-left   (get-in params [size :margin-left])
        hole-size     (get-in params [size :hole-size])
        border-radius (get-in params [size :border-radius (border-type type)])
        hole-radius   (get-in params [size :hole-radius (border-type type)])
        hole-x        (get-in params [size :hole-x])
        hole-y        (get-in params [size :hole-y])]
    [rn/view {:style {:flex-direction :row}}
     (for [index (range (if (> number 4) 3 number))]
       ^{:key (str index number)}
       [list-item index type size (get items-arr index) number
        margin-left hole-size hole-radius hole-x hole-y border-radius])
     (when (> number 4)
       [overflow-label
        {:label              (- number 3)
         :size               size
         :blur?              blur?
         :border-radius      border-radius
         :margin-left        margin-left
         :theme              theme
         :more-than-99-label more-than-99-label}])]))

(def view (quo.theme/with-theme view-internal))
