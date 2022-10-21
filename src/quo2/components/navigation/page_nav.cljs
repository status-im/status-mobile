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
           {:color color}
           {:no-color true})))

(defn render-left-section [{:keys [on-press icon
                                   icon-color icon-background-color]}
                           put-middle-section-on-left?]
  [rn/view {:style (merge
                    icon-styles
                    {:background-color icon-background-color
                     :width            32
                     :height           32}
                    (when put-middle-section-on-left? {:margin-right 5}))}
   [rn/touchable-opacity {:on-press on-press}
    [icons/icon icon (icon-props icon-color :big)]]])

(defn- mid-section-comp
  [{:keys [description-user-icon horizontal-description? text-secondary-color align-mid? text-color icon main-text type description]}]
  [rn/view {:style (assoc
                    centrify-style
                    :flex-direction    :row
                    :margin-horizontal 2)}
   (when (or (and (not horizontal-description?)
                  align-mid?
                  (not= :text-with-description type))
             (and description-user-icon
                  (not icon)))
     [rn/image {:source {:uri description-user-icon}
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
     main-text]
    (when description
      [text/text {:size   :paragraph-2
                  :weight :medium
                  :style  (cond-> {:padding-right 4
                                   :color         text-secondary-color
                                   :line-height   18}
                            horizontal-description? (assoc :margin-left 4 :margin-top 2))}
       description])]])

(defn- render-mid-section
  [{:keys [horizontal-description? one-icon-align-left? type left-align?
           main-text right-icon main-text-icon-color left-icon] :as props}]
  (let [text-color                (if (colors/dark?) colors/neutral-5 colors/neutral-95)
        text-secondary-color      (if (colors/dark?) colors/neutral-40 colors/neutral-50)
        component-instance [mid-section-comp (assoc props :text-secondary-color text-secondary-color)]]
    [rn/view {:style (merge
                      (if left-align?
                        align-left
                        centrify-style)
                      {:flex                1
                       :margin-left         4
                       :text-align-vertical :center})}
     (case type
       :text-only [text/text {:size   :paragraph-1
                              :weight :semi-bold
                              :style  {:color text-color}}
                   main-text]
       :text-with-two-icons [rn/view {:style (assoc centrify-style :flex-direction :row)}
                             [icons/icon left-icon
                              (icon-props main-text-icon-color :big)]
                             [text/text {:size   :paragraph-1
                                         :weight :semi-bold
                                         :style  {:padding-horizontal 4
                                                  :color              text-color}}
                              main-text]

                             [icons/icon right-icon
                              (icon-props main-text-icon-color :big)]]
       :text-with-one-icon   [rn/view {:style {:flex-direction :row}}
                              (if one-icon-align-left?
                                [rn/view {:style {:flex-direction :row
                                                  :align-items    :center}}
                                 (when horizontal-description?
                                   [icons/icon left-icon
                                    (icon-props main-text-icon-color :big)])
                                 component-instance]
                                [rn/view {:style {:flex-direction :row
                                                  :align-items :center}}
                                 component-instance
                                 (when horizontal-description?
                                   [icons/icon left-icon
                                    (icon-props main-text-icon-color :big)])])]
       :text-with-description component-instance)]))

(defn- right-section-icon
  [{:keys [background-color icon icon-color push-to-the-left?] :or {push-to-the-left? false}}]
  [rn/view {:style (assoc
                    icon-styles
                    :background-color background-color
                    :width            32
                    :height           32
                    :margin-right (if push-to-the-left? 8 0))}
   [icons/icon icon (icon-props icon-color :big)]])

(defn- render-right-section [right-section-buttons]
  [rn/view {:style (assoc
                    centrify-style
                    :flex-direction :row
                    :flex           1
                    :justify-content :flex-end)}
   (let [last-icon-index (-> right-section-buttons count dec)]
     (map-indexed (fn [index {:keys [icon background-color icon-color on-press]}]
                    ^{:key index}
                    [rn/touchable-opacity {:on-press on-press}
                     [right-section-icon
                      {:icon             icon
                       :background-color background-color
                       :icon-color       icon-color
                       :push-to-the-left? (not= index last-icon-index)}]])
                  right-section-buttons))])

(defn page-nav
  "[page-nav opts]
   opts
   { :one-icon-align-left?    true/false
     :horizontal-description? true/false
     :align-mid?              true/false
     :page-nav-color          color
     :page-nav-background-uri image-uri
     :mid-section 
     {:type                  one-of :text-only :text-with-two-icons :text-with-one-icon :text-with-description
      :icon                  icon
      :main-text             string
      :left-icon             icon       
      :right-icon            icon 
      :description           string
      :description-color     color
      :description-icon      icon
      :description-user-icon icon
      :main-text-icon-color  color
     }
     :left-section 
     {:on-press              event
      :icon                  icon 
      :icon-color            color
      :icon-background-color color
     }
     :right-section-buttons vector of 
      {:on-press              event
       :icon                  icon
       :icon-color            color 
       :icon-background-color color
      }
   }
  "
  [{:keys [one-icon-align-left? horizontal-description? align-mid?
           page-nav-color page-nav-background-uri
           mid-section
           left-section
           right-section-buttons]}]
  (let [{:keys [height width]}      (dimensions/window)
        put-middle-section-on-left? (or align-mid?
                                        (> (count right-section-buttons) 1))
        mid-section-props
        {:type                    (:type mid-section)
         :horizontal-description? horizontal-description?
         :main-text               (:main-text mid-section)
         :main-text-icon-color    (:main-text-icon-color mid-section)
         :one-icon-align-left?    one-icon-align-left?
         :right-icon              (:right-icon mid-section)
         :icon                    (:icon mid-section)
         :left-icon               (:left-icon mid-section)}]
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
      (render-left-section left-section put-middle-section-on-left?)
      (when put-middle-section-on-left?
        [render-mid-section (assoc mid-section-props
                                   :left-align?                       true
                                   :description           (:description mid-section)
                                   :description-color     (:description-color mid-section)
                                   :description-icon      (:description-icon mid-section)
                                   :align-mid?                        align-mid?
                                   :description-user-icon (:description-user-icon mid-section))])]
     (when-not put-middle-section-on-left?
       [render-mid-section mid-section-props])
     (render-right-section right-section-buttons)]))
