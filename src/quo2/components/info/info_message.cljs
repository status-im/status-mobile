(ns quo2.components.info.info-message
  (:require [quo2.components.icon :as quo2.icons]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [react-native.core :as rn]))

(def themes
  {:light {:default colors/neutral-50
           :success colors/success-50
           :error   colors/danger-50}
   :dark  {:default colors/neutral-40
           :success colors/success-60
           :error   colors/danger-60}})

(defn get-color
  [key]
  (get-in themes [(theme/get-theme) key]))

(defn info-message
  "[info-message opts \"message\"]
  opts
  {:type           :default/:success/:error
   :size           :default/:tiny
   :icon           :i/info ;; info message icon
   :text-color     colors/white     ;; text color override
   :icon-color     colors/white    ;; icon color override
   :no-icon-color? false       ;; disable tint color for icon"
  [{:keys [type size icon text-color icon-color no-icon-color? style]} message]
  (let [weight          (if (= size :default) :regular :medium)
        icon-size       (if (= size :default) 16 12)
        icon-margin-top (if (= size :default) 2 3)
        size            (if (= size :default) :paragraph-2 :label)
        text-color      (or text-color (get-color type))
        icon-color      (or icon-color text-color)]
    [rn/view
     {:style (merge {:flex-direction :row}
                    style)}
     [quo2.icons/icon icon
      {:color           icon-color
       :no-color        no-icon-color?
       :size            icon-size
       :container-style {:margin-top icon-margin-top}}]
     [text/text
      {:size   size
       :weight weight
       :style  {:color             text-color
                :margin-horizontal 8}} message]]))
