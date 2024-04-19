(ns quo.components.info.info-message
  (:require
    [quo.components.icon :as quo.icons]
    [quo.components.markdown.text :as text]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn get-color
  [k theme]
  (case k
    :success (colors/resolve-color :success theme)
    :error   (colors/resolve-color :danger theme)
    :warning (colors/resolve-color :warning theme)
    (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)))

(defn info-message
  "[info-message opts \"message\"]
  opts
  {:type           :default/:success/:error
   :size           :default/:tiny
   :icon           :i/info       ;; info message icon
   :text-color     colors/white  ;; text color override
   :icon-color     colors/white  ;; icon color override
   :no-icon-color? false         ;; disable tint color for icon"
  [{:keys [type size icon text-color icon-color no-icon-color? style accessibility-label
           container-style]} message]
  (let [theme      (quo.theme/use-theme)
        weight     (if (= size :default) :regular :medium)
        icon-size  (if (= size :default) 16 12)
        size       (if (= size :default) :paragraph-2 :label)
        text-color (or text-color (get-color type theme))
        icon-color (or icon-color text-color)]
    [rn/view
     {:style (merge {:flex-direction :row
                     :align-items    :center}
                    style
                    container-style)}
     [quo.icons/icon icon
      {:color    icon-color
       :no-color no-icon-color?
       :size     icon-size}]
     [text/text
      {:accessibility-label accessibility-label
       :size                size
       :weight              weight
       :style               {:color             text-color
                             :margin-horizontal 4}} message]]))
