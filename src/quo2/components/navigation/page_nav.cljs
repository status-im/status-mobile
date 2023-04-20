(ns quo2.components.navigation.page-nav
  (:require [clojure.string :as string]
            [quo2.components.avatars.user-avatar.view :as user-avatar]
            [quo2.components.buttons.button :as button]
            [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]))

(def ^:private centrify-style
  {:display         :flex
   :justify-content :center
   :align-items     :center})

(def ^:private align-left (assoc centrify-style :align-items :flex-start))

(defn- big? [size] (= size :big))

(defn- icon-props
  [color size]
  (merge {:size            20
          :container-style {:width  (if (big? size)
                                      20
                                      16)
                            :height (if (big? size)
                                      20
                                      16)}}
         (if-not (string/blank? color)
           {:color color}
           {:no-color true})))

(defn left-section-view
  [{:keys [on-press icon accessibility-label type icon-background-color icon-override-theme]
    :or   {type :grey}}
   put-middle-section-on-left?]
  [rn/view {:style (when put-middle-section-on-left? {:margin-right 5})}
   [button/button
    {:on-press                  on-press
     :icon                      true
     :type                      type
     :size                      32
     :accessibility-label       accessibility-label
     :override-theme            icon-override-theme
     :override-background-color icon-background-color}
    icon]])

(defn- mid-section-comp
  [{:keys [description-img description-user-icon horizontal-description?
           text-secondary-color align-mid? text-color icon main-text type description]}]
  [rn/view
   {:style (assoc centrify-style
                  :flex-direction    :row
                  :margin-horizontal 2)}
   (when (or (and (not horizontal-description?)
                  align-mid?
                  (not= :text-with-description type))
             (and (or description-img description-user-icon)
                  (not icon)))
     (if description-img
       [rn/view {:margin-right 8}
        [description-img]]
       [rn/image
        {:source {:uri description-user-icon}
         :style  {:width         32
                  :height        32
                  :border-radius 32
                  :margin-right  8}}]))
   [rn/view
    {:style {:flex-direction (if horizontal-description?
                               :row
                               :column)}}
    [text/text
     {:size   :paragraph-1
      :weight :semi-bold
      :style  {:color       text-color
               :line-height 21}}
     main-text]
    (when description
      [text/text
       {:size   :paragraph-2
        :weight :medium
        :style  (cond-> {:padding-right 4
                         :color         text-secondary-color
                         :line-height   18}
                  horizontal-description? (assoc :margin-left 4 :margin-top 2))}
       description])]])

(defn- mid-section-view
  [{:keys [horizontal-description? one-icon-align-left? type left-align?
           main-text right-icon main-text-icon-color left-icon on-press avatar]
    :as   props}]
  (let [text-color           (if (colors/dark?) colors/neutral-5 colors/neutral-95)
        text-secondary-color (if (colors/dark?) colors/neutral-40 colors/neutral-50)
        component-instance   [mid-section-comp (assoc props :text-secondary-color text-secondary-color)]]
    [rn/touchable-opacity {:on-press on-press}
     [rn/view
      {:style (merge
               (if left-align?
                 align-left
                 centrify-style)
               {:flex                1
                :margin-left         4
                :text-align-vertical :center})}
      (case type
        :text-only             [text/text
                                {:size   :paragraph-1
                                 :weight :semi-bold
                                 :style  {:color text-color}}
                                main-text]
        :user-avatar           [rn/view {:style (assoc centrify-style :flex-direction :row)}
                                [user-avatar/user-avatar avatar]
                                [text/text
                                 {:size   :paragraph-1
                                  :weight :semi-bold
                                  :style  {:padding-horizontal 4
                                           :color              text-color}}
                                 main-text]]
        :text-with-two-icons   [rn/view {:style (assoc centrify-style :flex-direction :row)}
                                [icons/icon left-icon
                                 (icon-props main-text-icon-color :big)]
                                [text/text
                                 {:size   :paragraph-1
                                  :weight :semi-bold
                                  :style  {:padding-horizontal 4
                                           :color              text-color}}
                                 main-text]
                                [icons/icon right-icon
                                 (icon-props main-text-icon-color :big)]]
        :text-with-one-icon    [rn/view {:style {:flex-direction :row}}
                                (if one-icon-align-left?
                                  [rn/view
                                   {:style {:flex-direction :row
                                            :align-items    :center}}
                                   (when horizontal-description?
                                     [icons/icon left-icon
                                      (icon-props main-text-icon-color :big)])
                                   component-instance]
                                  [rn/view
                                   {:style {:flex-direction :row
                                            :align-items    :center}}
                                   component-instance
                                   (when horizontal-description?
                                     [icons/icon left-icon
                                      (icon-props main-text-icon-color :big)])])]
        :text-with-description component-instance)]]))

(defn- right-section-view
  [right-section-buttons]
  [rn/view
   {:style (assoc centrify-style
                  :flex-direction  :row
                  :justify-content :flex-end)}
   (let [last-icon-index (-> right-section-buttons count dec)]
     (map-indexed (fn [index
                       {:keys [icon on-press type style icon-override-theme accessibility-label label]
                        :or   {type :grey}}]
                    ^{:key index}
                    [rn/view
                     (cond-> {:style (assoc style
                                            :margin-right
                                            (if (= index last-icon-index) 0 8))}
                       accessibility-label (assoc :accessibility-label accessibility-label
                                                  :accessible          true))
                     [button/button
                      {:on-press       on-press
                       :icon           (not label)
                       :type           type
                       :before         (when label icon)
                       :size           32
                       :override-theme icon-override-theme}
                      (if label label icon)]])
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
     {:type                  one-of :text-only :text-with-two-icons :text-with-one-icon :text-with-description :user-avatar
      :icon                  icon
      :main-text             string
      :left-icon             icon       
      :right-icon            icon 
      :description           string
      :description-color     color
      :description-icon      icon
      :description-user-icon icon
      :description-img a render prop which will be used in place of :description-user-icon
      :main-text-icon-color  color
     }
     :left-section 
     {:type                  button-type
      :on-press              event
      :icon                  icon
      :icon-override-theme   :light/:dark
     }
     :right-section-buttons vector of 
      {:type                  button-type
       :on-press              event
       :icon                  icon
       :icon-override-theme   :light/:dark
      }
   }
  "
  [{:keys [container-style one-icon-align-left? horizontal-description?
           align-mid? page-nav-color page-nav-background-uri
           mid-section
           left-section
           right-section-buttons]}]
  (let [put-middle-section-on-left? (or align-mid?
                                        (> (count right-section-buttons) 1))
        mid-section-props
        {:type                    (:type mid-section)
         :horizontal-description? horizontal-description?
         :description-img         (:description-img mid-section)
         :main-text               (:main-text mid-section)
         :main-text-icon-color    (:main-text-icon-color mid-section)
         :one-icon-align-left?    one-icon-align-left?
         :right-icon              (:right-icon mid-section)
         :icon                    (:icon mid-section)
         :left-icon               (:left-icon mid-section)
         :avatar                  (:avatar mid-section)}]
    [rn/view
     {:style (cond-> (merge {:display            :flex
                             :flex-direction     :row
                             ;;  iPhone 11 Pro's height in Figma divided by Component height 56/1125
                             :align-items        :center
                             :padding-horizontal 20
                             :height             56
                             :justify-content    :space-between}
                            container-style)
               page-nav-background-uri (assoc :background-color page-nav-color)
               page-nav-color          (assoc :background page-nav-background-uri))}
     [rn/view
      {:style {:flex           1
               :flex-direction :row
               :align-items    :center}}
      (when left-section
        [left-section-view left-section put-middle-section-on-left?])
      (when mid-section
        (cond
          put-middle-section-on-left?
          [mid-section-view
           (assoc mid-section-props
                  :left-align?           true
                  :description           (:description mid-section)
                  :description-color     (:description-color mid-section)
                  :description-icon      (:description-icon mid-section)
                  :align-mid?            align-mid?
                  :description-user-icon (:description-user-icon mid-section))]

          (not put-middle-section-on-left?)
          [mid-section-view mid-section-props]))]
     [right-section-view right-section-buttons]]))
