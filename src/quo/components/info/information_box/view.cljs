(ns quo.components.info.information-box.view
  (:require
    [clojure.string :as string]
    [quo.components.buttons.button.view :as button]
    [quo.components.icon :as icons]
    [quo.components.info.information-box.style :as style]
    [quo.components.markdown.text :as text]
    [quo.context]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]))

(defn- info-type->button-type
  [type]
  (if (= type :error)
    :danger
    :primary))

(defn- close-button
  [{:keys [theme on-close colors-map]}]
  [rn/touchable-opacity
   {:on-press            on-close
    :hit-slop            {:top 3 :right 3 :bottom 3 :left 3}
    :accessibility-label :information-box-close-button}
   [icons/icon :i/close
    {:size            12
     :color           (style/get-color colors-map theme :close-button)
     :container-style style/close-button}]])

(defn- content
  [{:keys [theme type button-label on-button-press message colors-map customization-color]}]
  [rn/view {:style {:flex 1}}
   [text/text
    {:size  :paragraph-2
     :style (style/content-text colors-map theme type)}
    message]
   (when (not (string/blank? button-label))
     [button/button
      {:type                (info-type->button-type type)
       :accessibility-label :information-box-action-button
       :size                24
       :customization-color customization-color
       :on-press            on-button-press
       :container-style     style/content-button}
      button-label])])

(defn view
  "[view opts \"message\"]
   opts
   {:type            :default/:informative/:error
    :closed?         bool (false)  ;; Information box's state
    :icon            keyword, required (:i/info)
    :icon-size       int (16)
    :no-icon-color?  bool (false)
    :style           map
    :button-label    string
    :on-button-press function
    :on-close        function"
  [{:keys [type closed? blur? icon style button-label
           on-button-press on-close no-icon-color? icon-size customization-color]
    :or   {customization-color :primary}}
   message]
  (when-not closed?
    (let [quo-theme               (quo.context/use-theme)
          theme                   (if blur? :shell quo-theme)
          customization-color-hex (colors/resolve-color customization-color theme)
          colors-map              (style/get-colors-map customization-color-hex)
          include-button?         (not (string/blank? button-label))]
      [rn/view
       {:accessibility-label :information-box
        :style               (merge (style/container {:theme               theme
                                                      :colors-map          colors-map
                                                      :customization-color customization-color
                                                      :type                type
                                                      :include-button?     include-button?})
                                    style)}
       [icons/icon (or icon :i/info)
        {:color           (style/get-color-by-type colors-map theme type :icon)
         :no-color        no-icon-color?
         :size            (or icon-size 16)
         :container-style style/icon}]
       [content
        {:theme               theme
         :type                type
         :button-label        button-label
         :on-button-press     on-button-press
         :colors-map          colors-map
         :customization-color customization-color
         :message             message}]
       (when on-close
         [close-button {:theme theme :colors-map colors-map :on-close on-close}])])))
