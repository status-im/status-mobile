(ns quo2.components.navigation.page-nav
  (:require [quo.react-native :as rn]
            [quo2.foundations.colors :as colors]
            [quo2.components.icon :as icons]
            [status-im.utils.dimensions :as dimensions]
            [clojure.string :as string]
            [quo2.components.markdown.text :as text]))

(def ^:private centrify-style
  {:display         :flex
   :justify-content :center
   :align-items     :center})

(def ^:private align-left (assoc centrify-style :align-items :flex-start))

(def ^:private icon-styles (assoc centrify-style :width 32 :height 32 :border-radius 10))

(defn- big? [size] (= size :big))

(defn- icon-props [color size]
  (merge {:size      20
          :container-style {:width  (if (big? size)
                                      20
                                      16)
                            :height (if (big? size)
                                      20
                                      16)}}
         (if-not (string/blank? color)
           {:color    color}
           {:no-color true})))

(defn- mid-section-comp
  [{:keys [mid-section-description-user-icon horizontal-description? text-secondary-color align-mid? text-color mid-section-icon mid-section-main-text mid-section-type mid-section-description]}]
  [rn/view {:style (assoc
                    centrify-style
                    :flex-direction    :row
                    :margin-horizontal 2)}
   (when (or (and (not horizontal-description?)
                  align-mid?
                  (not= :text-with-description mid-section-type))
             (and mid-section-description-user-icon
                  (not mid-section-icon)))
     [rn/image {:source {:uri mid-section-description-user-icon}
                :style  {:width         32
                         :height        32
                         :border-radius 32
                         :margin-right  8}}])
   [rn/view {:style {:flex-direction (if horizontal-description?
                                       :row
                                       :column)}}
    [text/text {:size   :paragraph-1
                :weight :semi-bold
                :style  {:color       text-color
                         :line-height 21}}
     mid-section-main-text]
    (when mid-section-description
      [text/text {:size   :paragraph-2
                  :weight :medium
                  :style  (cond-> {:padding-right 4
                                   :color         text-secondary-color
                                   :line-height   18}
                            horizontal-description? (assoc :margin-left 4 :margin-top 2))}
       mid-section-description])]])

(defn- mid-section
  [{:keys [horizontal-description? one-icon-align-left? mid-section-type left-align? mid-section-main-text mid-section-right-icon mid-section-main-text-icon-color mid-section-left-icon] :as mid-section-props}]
  (let [text-color                (if (colors/dark?) colors/neutral-5 colors/neutral-95)
        text-secondary-color      (if (colors/dark?) colors/neutral-40 colors/neutral-50)
        mid-section-comp-instance [mid-section-comp (assoc mid-section-props :text-secondary-color text-secondary-color)]]
    [rn/view {:style (merge
                      (if left-align?
                        align-left
                        centrify-style)
                      {:flex                1
                       :margin-left         4
                       :text-align-vertical :center})}
     (case mid-section-type
       :text-only [text/text {:size   :paragraph-1
                              :weight :semi-bold
                              :style  {:color text-color}}
                   mid-section-main-text]
       :text-with-two-icons [rn/view {:style (assoc centrify-style :flex-direction :row)}
                             [icons/icon mid-section-left-icon
                              (icon-props mid-section-main-text-icon-color :big)]
                             [text/text {:size   :paragraph-1
                                         :weight :semi-bold
                                         :style  {:padding-horizontal 4
                                                  :color              text-color}}
                              mid-section-main-text]

                             [icons/icon mid-section-right-icon
                              (icon-props mid-section-main-text-icon-color :big)]]
       :text-with-one-icon   [rn/view {:style {:flex-direction :row}}
                              (if one-icon-align-left?
                                [rn/view {:style {:flex-direction :row
                                                  :align-items    :center}}
                                 (when horizontal-description?
                                   [icons/icon mid-section-left-icon
                                    (icon-props mid-section-main-text-icon-color :big)])
                                 mid-section-comp-instance]
                                [rn/view {:style {:flex-direction :row
                                                  :align-items :center}}
                                 mid-section-comp-instance
                                 (when horizontal-description?
                                   [icons/icon mid-section-left-icon
                                    (icon-props mid-section-main-text-icon-color :big)])])]
       :text-with-description mid-section-comp-instance)]))

(defn- right-section-icon
  [{:keys [background-color icon icon-color push-to-the-left?] :or {push-to-the-left? false}}]
  [rn/view {:style (assoc
                    icon-styles
                    :background-color background-color
                    :width            32
                    :height           32
                    :margin-right (if push-to-the-left? 8 0))}
   [icons/icon icon (icon-props icon-color :big)]])

(defn page-nav
  [{:keys [one-icon-align-left? horizontal-description? align-mid? page-nav-color page-nav-background-uri mid-section-type mid-section-icon mid-section-main-text mid-section-left-icon mid-section-right-icon mid-section-description mid-section-description-color mid-section-description-icon mid-section-description-user-icon mid-section-main-text-icon-color left-section-icon left-section-icon-color left-section-icon-background-color right-section-icons]}]
  (let [{:keys [height width]}      (dimensions/window)
        put-middle-section-on-left? (or align-mid?
                                        (> (count right-section-icons) 1))
        mid-section-props {:mid-section-type                 mid-section-type
                           :horizontal-description?          horizontal-description?
                           :mid-section-main-text            mid-section-main-text
                           :one-icon-align-left?             one-icon-align-left?
                           :mid-section-right-icon           mid-section-right-icon
                           :mid-section-icon                 mid-section-icon
                           :mid-section-main-text-icon-color mid-section-main-text-icon-color
                           :mid-section-left-icon            mid-section-left-icon}]
    [rn/view {:style (cond->
                      {:display            :flex
                       :flex-direction     :row
                       :width              width
                       :height             (* 0.0497 height)
                      ;;  iPhone 11 Pro's height in Figma divided by Component height 56/1125
                       :align-items        :center
                       :padding-horizontal 20
                       :justify-content    :space-between}
                       page-nav-background-uri (assoc :background-color page-nav-color)
                       page-nav-color (assoc :background page-nav-background-uri))}
     [rn/view {:style {:flex           1
                       :flex-direction :row
                       :align-items    :center}}
      [rn/view {:style (merge
                        icon-styles
                        {:background-color left-section-icon-background-color
                         :width            32
                         :height           32}
                        (when put-middle-section-on-left? {:margin-right 5}))}
       [icons/icon left-section-icon (icon-props left-section-icon-color :big)]]
      (when put-middle-section-on-left?
        [mid-section (assoc mid-section-props
                            :left-align?                       true
                            :mid-section-description           mid-section-description
                            :mid-section-description-color     mid-section-description-color
                            :mid-section-description-icon      mid-section-description-icon
                            :align-mid?                        align-mid?
                            :mid-section-description-user-icon mid-section-description-user-icon)])]
     (when-not put-middle-section-on-left?
       [mid-section mid-section-props])
     [rn/view {:style (assoc
                       centrify-style
                       :flex-direction :row
                       :flex           1
                       :justify-content :flex-end)}
      (let [last-icon-index (- (count right-section-icons) 1)]
        (map-indexed (fn [index {:keys [icon background-color icon-color]}]
                       ^{:key index}
                       [right-section-icon {:icon             icon
                                            :background-color background-color
                                            :icon-color       icon-color
                                            :push-to-the-left? (if (= index last-icon-index) false true)}])
                     right-section-icons))]]))
