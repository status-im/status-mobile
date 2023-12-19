(ns legacy.status-im.ui.components.text-input
  (:require
    [clojure.spec.alpha :as s]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.spacing :as spacing]
    [legacy.status-im.ui.components.text :as text]
    [legacy.status-im.ui.components.tooltip :as tooltip]
    [legacy.status-im.ui.components.typography :as typography]
    [oops.core :refer [ocall]]
    [react-native.core :as rn] ;; TODO(Ferossgp): Move icon component to lib
    [react-native.platform :as platform]
    [reagent.core :as reagent]))

;; NOTE(Ferossgp): Refactor with hooks when available
;; We track all currently mounted text input refs
;; in a ref-to-defaultValue map
;; so that we can clear them (restore their default values)
;; when global react-navigation's onWillBlur event is invoked
(defonce text-input-refs (atom {}))

(s/def ::multiline boolean?)
(s/def ::secure-text-entry boolean?)
(s/def ::show-cancel boolean?)
(s/def ::label
  (s/nilable (s/or :string    string?
                   :component vector?)))
(s/def ::cancel-label (s/nilable string?))
(s/def ::default-value (s/nilable string?))
(s/def ::placeholder (s/nilable string?))
(s/def ::keyboard-type
  (s/nilable (s/or :string  string?
                   :keyword keyword?))) ; TODO: make set
(s/def ::accessibility-label
  (s/nilable (s/or :string  string?
                   :keyword keyword?)))
(s/def ::on-focus fn?)
(s/def ::on-blur fn?)
(s/def ::on-press fn?)

(s/def ::accessory
  (s/keys :opt-un [::on-press
                   ::icon
                   ::component]))
(s/def ::after (s/nilable ::accessory))
(s/def ::before (s/nilable ::accessory))

(s/def ::style (s/nilable map?))
(s/def ::input-style ::style)
(s/def ::container-style ::style)

(s/def ::text-input
  (s/keys :opt-un
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
           ::container-style
           ::show-cancel
           ::accessibility-label
           ::bottom-value
           ::secure-text-entry]))

(defn check-spec
  [spec prop]
  (if (s/valid? spec prop)
    true
    (do
      (s/explain spec prop)
      false)))

(def height 44)                         ; 22 line-height + 11*2 vertical padding
(def multiline-height 88)               ; 3 * 22 three line-height + 11* vertical padding

(defn label-style
  []
  {:margin-bottom (:tiny spacing/spacing)})

(defn text-input-row-style
  []
  {:flex-direction :row
   :align-items    :center})

(defn text-input-view-style
  [style]
  (merge {:border-radius    8
          :flex-direction   :row
          :flex             1
          :align-items      :center
          :background-color (:ui-01 @colors/theme)}
         style))

(defn text-input-style
  [multiline input-style monospace before after]
  (merge (if monospace
           typography/monospace
           typography/font-regular)
         {:padding-top         11
          :padding-bottom      11
          :font-size           15
          :margin              0
          :text-align-vertical :center
          :flex                1
          :color               (:text-01 @colors/theme)
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

(defn cancel-style
  []
  {:margin-left     (:tiny spacing/spacing)
   :padding-left    (:tiny spacing/spacing)
   :justify-content :center
   :align-self      :stretch})

(defn accessory-style
  []
  (merge (:base spacing/padding-horizontal)
         {:flex            1
          :justify-content :center}))

(defn accessory-element
  [{:keys [icon component icon-opts style accessibility-label on-press]}]
  (let [el (if on-press
             rn/touchable-opacity
             rn/view)]
    [el
     (merge {:style {:align-self :stretch}}
            (when on-press
              {:on-press on-press}))
     [rn/view
      (merge {:style (merge (accessory-style)
                            style)}
             (when accessibility-label
               {:accessibility-label accessibility-label}))
      (cond
        icon
        [icons/icon icon
         (merge {:color (:icon-01 @colors/theme)}
                icon-opts)]
        component
        component

        :else
        nil)]]))

(defn text-input-raw
  []
  (let [focused (reagent/atom nil)
        visible (reagent/atom false)
        ref     (atom nil)
        blur    (fn []
                  (some-> @ref
                          (ocall "blur")))]
    (fn
      [{:keys [label multiline error style input-style keyboard-type before after
               cancel-label on-focus on-blur show-cancel accessibility-label
               bottom-value secure-text-entry container-style get-ref on-cancel
               monospace auto-complete-type auto-correct]
        :or   {cancel-label "Cancel"}
        :as   props}]
      {:pre [(check-spec ::text-input props)]}
      (let [show-cancel   (if (nil? show-cancel)
                            ;; Enabled by default on iOs and disabled on Android
                            platform/ios?
                            show-cancel)
            after         (cond
                            (and secure-text-entry @visible)
                            {:icon     :main-icons/hide
                             :on-press #(reset! visible false)}

                            (and secure-text-entry (not @visible))
                            {:icon     :main-icons/show
                             :on-press #(reset! visible true)}

                            :else after)
            secure        (boolean (and secure-text-entry (not @visible))) ; must be a boolean to work on
                                                                           ; iOS
            auto-complete (cond
                            (= keyboard-type :visible-password)
                            :off

                            secure-text-entry
                            :password

                            :else
                            auto-complete-type)
            auto-correct  (and (not= keyboard-type :visible-password)
                               (not secure-text-entry)
                               auto-correct)
            on-cancel     (fn []
                            (when on-cancel
                              (on-cancel))
                            (blur))
            keyboard-type (cond
                            (and platform/ios? (= keyboard-type :visible-password))
                            :default

                            ; the correct approach on Android would be keep secure-text-entry on set
                            ; keyboard type
                            ; to visible-password. But until
                            ; https://github.com/facebook/react-native/issues/27946
                            ; is solved that's the second best way.
                            (and platform/android? secure-text-entry)
                            :default

                            :else
                            keyboard-type)]
        [rn/view {:style container-style}
         (when label
           [text/text {:style (label-style)}
            label])
         [rn/view {:style (text-input-row-style)}
          [rn/view
           {:style                       (text-input-view-style style)
            :important-for-accessibility (if secure-text-entry
                                           :no-hide-descendants
                                           :auto)}
           (when before
             [accessory-element before])
           [rn/text-input
            (merge
             {:style                   (text-input-style multiline input-style monospace before after)
              :ref                     (fn [r]
                                         (reset! ref r)
                                         (when get-ref (get-ref r)))
              :placeholder-text-color  (:text-02 @colors/theme)
              :underline-color-android :transparent
              :auto-capitalize         :none
              :secure-text-entry       secure
              :auto-correct            auto-correct
              :auto-complete-type      auto-complete
              :on-focus                (fn [evt]
                                         (when on-focus (on-focus evt))
                                         (when show-cancel
                                           (rn/configure-next (:ease-in-ease-out
                                                               rn/layout-animation-presets)))
                                         (reset! focused true))
              :on-blur                 (fn [evt]
                                         (when on-blur (on-blur evt))
                                         (when show-cancel
                                           (rn/configure-next (:ease-in-ease-out
                                                               rn/layout-animation-presets)))
                                         (reset! focused false))
              :keyboard-type           keyboard-type}
             (when (and platform/ios? (not after))
               {:clear-button-mode :while-editing})
             (dissoc props
              :style
              :keyboard-type
              :on-focus
              :on-blur
              :secure-text-entry
              :ref
              :get-ref
              :auto-correct
              :auto-complete-type))]
           (when after
             [accessory-element after])]
          (when (and show-cancel
                     (not multiline)
                     @focused)
            [rn/touchable-opacity
             {:style    (cancel-style)
              :on-press on-cancel}
             [text/text {:color :link} cancel-label]])
          (when error
            [tooltip/tooltip
             (merge {:bottom-value (if bottom-value bottom-value 0)}
                    (when accessibility-label
                      {:accessibility-label (str (name accessibility-label) "-error")}))
             [text/text
              {:color :negative
               :align :center
               :size  :small}
              error]])]]))))

;; TODO(Ferossgp): Refactor me when hooks available
(defn text-input
  [{:keys [preserve-input?]
    :as   props}]
  (if preserve-input?
    [text-input-raw props]
    (let [id (random-uuid)]
      (reagent/create-class
       {:component-will-unmount
        (fn []
          (swap! text-input-refs dissoc id))
        :reagent-render
        (fn [{:keys [get-ref default-value]
              :as   props}]
          [text-input-raw
           (merge props
                  {:get-ref (fn [r]
                              ;; Store input and its defaultValue
                              ;; one we receive a non-nil ref
                              (when r
                                (swap! text-input-refs assoc id {:ref r :value default-value}))
                              (when get-ref (get-ref r)))})])}))))
