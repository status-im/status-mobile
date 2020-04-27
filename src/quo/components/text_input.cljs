(ns quo.components.text-input
  (:require [clojure.spec.alpha :as s]
            [reagent.core :as reagent]
            [oops.core :refer [ocall]]
            [quo.react-native :as rn]
            ;; TODO(Ferossgp): Move icon component to lib
            [status-im.ui.components.icons.vector-icons :as icons]
            ;; TODO(Ferossgp): Move tooltip into lib
            [quo.components.tooltip :as tooltip]
            [quo.react :as react]
            [quo.platform :as platform]
            [quo.design-system.typography :as typography]
            [quo.design-system.spacing :as spacing]
            [quo.design-system.colors :as colors]
            [quo.components.text :as text]))

(s/def ::multiline boolean?)
(s/def ::secure-text-entry boolean?)
(s/def ::show-cancel boolean?)
(s/def ::label (s/nilable string?))
(s/def ::cancel-label (s/nilable string?))
(s/def ::default-value (s/nilable string?))
(s/def ::placeholder (s/nilable string?))
(s/def ::keyboard-type #{})
(s/def ::accessibility-label (s/nilable (s/or :string string? :keyword keyword?)))
(s/def ::on-focus fn?)
(s/def ::on-blur fn?)
(s/def ::on-press fn?)

(s/def ::accessory (s/keys :req-un [::icon]
                           :opt-un [::on-press]))
(s/def ::after (s/nilable ::accessory))
(s/def ::before (s/nilable ::accessory))

(s/def ::style (s/nilable map?))
(s/def ::input-style (s/nilable map?))

(s/def ::text-input (s/keys :opt-un
                            [::label
                             ::multiline
                             ::error
                             ::style
                             ::input-style
                             ::keyboard-type
                             ::before
                             ::after
                             ::cancel-label
                             ::on-focus
                             ::on-blur
                             ::show-cancel
                             ::accessibility-label
                             ::bottom-value
                             ::secure-text-entry]))

(defn check-spec [spec prop]
  (if (s/valid? spec prop)
    true
    (do
      (s/explain spec prop)
      false)))

;; TODO(Ferossgp): Check performance for android layout animations
(when (and platform/android?
           (aget rn/ui-manager "setLayoutAnimationEnabledExperimental"))
  (ocall rn/ui-manager "setLayoutAnimationEnabledExperimental" true))

(def height 44)                         ; 22 line-height + 11*2 vertical padding
(def multiline-height 88)               ; 3 * 22 three line-height + 11* vertical padding

(defn container-style [])

(defn label-style []
  {:margin-bottom (:tiny spacing/spacing)})

(defn text-input-row-style []
  {:flex-direction :row
   :align-items    :center})

(defn text-input-view-style [style]
  (merge {:border-radius    8
          :flex-direction   :row
          :flex             1
          :align-items      :center
          :background-color (:ui-01 @colors/theme)}
         style))

(defn text-input-style [multiline input-style before after]
  (merge typography/font-regular
         {:padding-top         11
          :padding-bottom      11
          :font-size           15
          :margin              0
          :text-align-vertical :center
          :flex                1
          :height              height}
         (when-not before
           {:padding-left (:base spacing/spacing)})
         (when-not after
           {:padding-right (:base spacing/spacing)})
         (when multiline
           {:text-align-vertical :top
            :line-height         22
            :height              multiline-height})
         input-style))

(defn cancel-style []
  {:margin-left     (:tiny spacing/spacing)
   :padding-left    (:tiny spacing/spacing)
   :justify-content :center
   :align-self      :stretch})

(defn accessory-style []
  (merge (:base spacing/padding-horizontal)
         {:flex            1
          :justify-content :center}))

(defn accessory-element [{:keys [icon icon-opts style accessibility-label on-press]}]
  (let [el (if on-press
             rn/touchable-opacity
             rn/view)]
    [el (merge {:style {:align-self :stretch}}
               (when on-press
                 {:on-press on-press}))
     [rn/view (merge {:style (merge (accessory-style)
                                    style)}
                     (when accessibility-label
                       {:accessibility-label accessibility-label}))
      [icons/icon icon (merge {:color (:icon-01 @colors/theme)}
                              icon-opts)]]]))

(defn text-input []
  (let [focused   (reagent/atom nil)
        visible   (reagent/atom false)
        ref       (react/create-ref)
        on-cancel (fn []
                    (some-> (react/current-ref ref) (ocall "blur")))]
    (fn [{:keys [label multiline error style input-style keyboard-type before after
                 cancel-label on-focus on-blur show-cancel accessibility-label
                 bottom-value secure-text-entry container-style]
          :or  {cancel-label "Cancel"
                show-cancel  true}
          :as  props}]
      {:pre [(check-spec ::text-input props)]}
      (let [after (cond
                    (and secure-text-entry @visible)
                    {:icon     :main-icons/hide
                     :on-press #(reset! visible false)}

                    (and secure-text-entry (not @visible))
                    {:icon     :main-icons/show
                     :on-press #(reset! visible true)}

                    :else after)
            secure (and secure-text-entry (not @visible))]
        [rn/view {:style container-style}
         (when label
           [text/text {:style (label-style)}
            label])
         [rn/view {:style (text-input-row-style)}
          [rn/view {:style (text-input-view-style style)}
           (when before
             [accessory-element before])
           [rn/text-input
            (merge {:style                   (text-input-style multiline input-style before after)
                    :ref                     ref
                    :placeholder-text-color  (:text-02 @colors/theme)
                    :color                   (:text-01 @colors/theme)
                    :underline-color-android :transparent
                    :auto-capitalize         :none
                    :secure-text-entry       secure
                    :on-focus                (fn [evt]
                                               (when on-focus (on-focus evt))
                                               (rn/configure-next (:ease-in-ease-out rn/layout-animation-presets))
                                               (reset! focused true))
                    :on-blur                 (fn [evt]
                                               (when on-blur (on-blur evt))
                                               (rn/configure-next (:ease-in-ease-out rn/layout-animation-presets))
                                               (reset! focused false))}
                   (when (and platform/ios? (not after))
                     {:clear-button-mode :while-editing})
                   (when (and platform/ios?
                              (not= keyboard-type "visible-password"))
                     {:keyboard-type keyboard-type})
                   (dissoc props
                           :style :keyboard-type :on-focus :on-blur
                           :secure-text-entry :ref))]
           (when after
             [accessory-element after])]
          (when (and platform/ios?
                     show-cancel
                     (not multiline)
                     @focused)
            [rn/touchable-opacity {:style    (cancel-style)
                                   :on-press on-cancel}
             [text/text {:color :link} cancel-label]])]
         (when error
           [tooltip/tooltip (merge {:bottom-value (cond bottom-value bottom-value
                                                        label        30 ; 22 line height 8 margin
                                                        )}
                                   (when accessibility-label
                                     {:accessibility-label (str (name accessibility-label) "-error")}))
            [text/text {:color :negative
                        :size  :small}
             error]])]))))
