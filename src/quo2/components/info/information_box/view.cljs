(ns quo2.components.info.information-box.view
  (:require [clojure.string :as string]
            [quo2.components.buttons.button :as button]
            [quo2.components.icon :as icons]
            [quo2.components.info.information-box.style :as style]
            [quo2.components.markdown.text :as text]
            [react-native.core :as rn]))

(defn- info-type->button-type
  [type]
  (if (= type :error)
    :danger
    :primary))

(defn- close-button
  [{:keys [on-close]}]
  [rn/touchable-opacity
   {:on-press            on-close
    :hit-slop            {:top 3 :right 3 :bottom 3 :left 3}
    :accessibility-label :information-box-close-button}
   [icons/icon :i/close
    {:size            12
     :color           (style/get-color :close-button)
     :container-style style/close-button}]])

(defn- content
  [{:keys [type button-label on-button-press message]}]
  [rn/view {:style {:flex 1}}
   [text/text
    {:size  :paragraph-2
     :style (style/content-text type)}
    message]
   (when (not (string/blank? button-label))
     [button/button
      {:type                (info-type->button-type type)
       :accessibility-label :information-box-action-button
       :size                24
       :on-press            on-button-press
       :style               style/content-button}
      button-label])])

(defn view
  "[view opts \"message\"]
   opts
   {:type            :default/:informative/:error
    :closable?       bool (false)  ;; Allow information box to be closed?
    :closed?         bool (false)  ;; Information box's state
    :icon            keyword, required (:i/info)
    :icon-size       int (16)
    :no-icon-color?  bool (false)
    :style           map
    :button-label    string
    :on-button-press function
    :on-close        function"
  [{:keys [type closable? closed? icon style button-label
           on-button-press on-close no-icon-color? icon-size]}
   message]
  (when-not closed?
    (let [include-button? (not (string/blank? button-label))]
      [rn/view
       {:accessibility-label :information-box
        :style               (merge (style/container {:type            type
                                                      :include-button? include-button?})
                                    style)}
       [icons/icon icon
        {:color           (style/get-color-by-type type :icon)
         :no-color        no-icon-color?
         :size            (or icon-size 16)
         :container-style style/icon}]
       [content
        {:type            type
         :button-label    button-label
         :on-button-press on-button-press
         :message         message}]
       (when closable?
         [close-button {:on-close on-close}])])))
