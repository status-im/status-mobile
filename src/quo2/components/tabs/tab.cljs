(ns quo2.components.tabs.tab
  (:require [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.components.notifications.notification-dot :as notification-dot]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [react-native.core :as rn]
            [react-native.svg :as svg]))

(def ^:const notification-dot-offset 2)

(def themes
  {:light {:default  {:background-color colors/neutral-20
                      :icon-color       colors/neutral-50
                      :label            {:style {:color colors/neutral-100}}}
           :active   {:background-color colors/neutral-50
                      :icon-color       colors/white
                      :label            {:style {:color colors/white}}}
           :disabled {:background-color colors/neutral-20
                      :icon-color       colors/neutral-50
                      :label            {:style {:color colors/neutral-100}}}}
   :dark  {:default  {:background-color colors/neutral-80
                      :icon-color       colors/neutral-40
                      :label            {:style {:color colors/white}}}
           :active   {:background-color colors/neutral-60
                      :icon-color       colors/white
                      :label            {:style {:color colors/white}}}
           :disabled {:background-color colors/neutral-80
                      :icon-color       colors/neutral-40
                      :label            {:style {:color colors/white}}}}})

(def themes-for-blur-background
  {:light {:default  {:background-color colors/neutral-80-opa-5
                      :icon-color       colors/neutral-80-opa-40
                      :label            {:style {:color colors/neutral-100}}}
           :active   {:background-color colors/neutral-80-opa-60
                      :icon-color       colors/white
                      :label            {:style {:color colors/white}}}
           :disabled {:background-color colors/neutral-80-opa-5
                      :icon-color       colors/neutral-80-opa-40
                      :label            {:style {:color colors/neutral-100}}}}
   :dark  {:default  {:background-color colors/white-opa-5
                      :icon-color       colors/white
                      :label            {:style {:color colors/white}}}
           :active   {:background-color colors/white-opa-20
                      :icon-color       colors/white
                      :label            {:style {:color colors/white}}}
           :disabled {:background-color colors/white-opa-5
                      :icon-color       colors/neutral-40
                      :label            {:style {:color colors/white}}}}})

(defn size->border-radius
  [size]
  (case size
    32 10
    28 8
    24 8
    20 6
    nil))

(defn size->padding-left
  [size]
  (case size
    32 12
    28 12
    24 8
    20 8
    nil))

(defn show-notification-dot?
  [notification-dot? size]
  (and notification-dot? (= size 32)))

(defn style-container
  [{:keys [size disabled background-color notification-dot?]}]
  (let [border-radius (size->border-radius size)
        padding       (size->padding-left size)]
    (merge {:height                    size
            :align-items               :center
            :justify-content           :flex-end
            :flex-direction            :row
            :border-top-left-radius    border-radius
            :border-bottom-left-radius border-radius
            :background-color          background-color
            :padding-left              padding}
           (when-not (show-notification-dot? notification-dot? size)
             {:padding-horizontal padding
              :border-radius      border-radius})
           (when disabled
             {:opacity 0.3}))))

(defn right-side-with-cutout
  [{:keys [height width background-color]}]
  [svg/svg
   {:width    width
    :height   height
    :view-box (str "0 0 " width " " height)
    :fill     :none}
   [svg/path
    {:fill-rule :evenodd
     :clip-rule :evenodd
     :fill      background-color
     :d
     "M11.4683 6.78094C11.004 6.92336 10.511 7 10 7C7.23858 7 5 4.76142 5 2C5 1.48904 5.07664 0.995988 5.21906 0.531702C4.68658 0.350857 4.13363 0.213283 3.56434 0.123117C2.78702 0 1.85801 0 0 0V32C1.85801 32 2.78702 32 3.56434 31.8769C7.84327 31.1992 11.1992 27.8433 11.8769 23.5643C12 22.787 12 21.858 12 20V12C12 10.142 12 9.21298 11.8769 8.43566C11.7867 7.86637 11.6491 7.31342 11.4683 6.78094Z"}]])

(defn tab
  "[tab opts \"label\"]
   opts
   {:type :primary/:secondary/:grey/:outline/:ghost/:danger
    :size 40/32/24
    :icon true/false
    :before :icon-keyword
    :after :icon-keyword}"
  [_ _]
  (fn
    [{:keys [accessibility-label
             active
             before
             blur?
             disabled
             id
             on-press
             override-theme
             size
             notification-dot?]
      :or   {size 32}}
     children]
    (let [state                                       (cond disabled :disabled
                                                            active   :active
                                                            :else    :default)
          {:keys [icon-color background-color label]}
          (get-in (if blur? themes-for-blur-background themes)
                  [(or override-theme (theme/get-theme)) state])]
      [rn/touchable-without-feedback
       (merge {:disabled            disabled
               :accessibility-label accessibility-label}
              (when on-press
                {:on-press (fn []
                             (on-press id))}))
       [rn/view {:style {:flex-direction :row}}
        (when (show-notification-dot? notification-dot? size)
          [rn/view
           {:style {:position :absolute
                    :z-index  1
                    :right    (- notification-dot/size notification-dot-offset)
                    :top      (- notification-dot-offset)}}
           [notification-dot/notification-dot]])
        [rn/view
         {:style (style-container {:size              size
                                   :disabled          disabled
                                   :background-color  background-color
                                   :notification-dot? notification-dot?})}
         (when before
           [rn/view
            [icons/icon before {:color icon-color}]])
         [rn/view
          (cond
            (string? children)
            [text/text
             (merge {:size            (case size
                                        24 :paragraph-2
                                        20 :label
                                        nil)
                     :weight          :medium
                     :number-of-lines 1}
                    label)
             children]
            (vector? children)
            children)]]
        (when (show-notification-dot? notification-dot? size)
          [right-side-with-cutout
           {:width            (size->padding-left size)
            :height           size
            :background-color background-color}])]])))
