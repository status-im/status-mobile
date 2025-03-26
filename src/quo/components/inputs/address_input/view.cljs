(ns quo.components.inputs.address-input.view
  (:require
    [quo.components.buttons.button.view :as button]
    [quo.components.icon :as icon]
    [quo.components.inputs.address-input.style :as style]
    [quo.context :as quo.context]
    [quo.foundations.colors :as colors]
    [react-native.clipboard :as clipboard]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [utils.i18n :as i18n]))

(defn- icon-color
  [blur? theme]
  (if blur?
    (colors/theme-colors colors/neutral-80-opa-30 colors/white-opa-10 theme)
    (colors/theme-colors colors/neutral-40 colors/neutral-60 theme)))

(defn- clear-button
  [{:keys [on-press blur? theme]}]
  [rn/touchable-opacity
   {:accessibility-label :clear-button
    :style               style/clear-icon-container
    :on-press            on-press}
   [icon/icon :i/clear
    {:color (icon-color blur? theme)
     :size  20}]])

(defn- loading-icon
  [blur? theme]
  [rn/view {:style style/clear-icon-container}
   [icon/icon :i/loading
    {:color (icon-color blur? theme)
     :size  20}]])

(defn- positive-state-icon
  [theme]
  [rn/view {:style style/clear-icon-container}
   [icon/icon :i/positive-state
    {:color (colors/resolve-color :success theme)
     :size  20}]])

(defn- get-placeholder-text-color
  [status theme blur?]
  (cond
    (and (= status :default) blur?)
    (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-30 theme)
    (and (= status :default) (not blur?))
    (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)
    (and (not= status :default) blur?)
    (colors/theme-colors colors/neutral-80-opa-20 colors/white-opa-20 theme)
    (and (not= status :default) (not blur?))
    (colors/theme-colors colors/neutral-30 colors/neutral-60 theme)))

(defn address-input
  [{:keys [default-value blur? on-change-text on-blur on-focus on-clear on-scan on-paste
           on-detect-ens on-detect-address on-detect-unclassified address-regex ens-regex
           valid-ens-or-address? container-style]}]
  (let [theme                  (quo.context/use-theme)
        [status set-status]    (rn/use-state :default)
        [value set-value]      (rn/use-state nil)
        [focused? set-focused] (rn/use-state false)
        on-change              (rn/use-callback
                                (fn [text]
                                  (let [address? (when address-regex
                                                   (boolean (re-matches address-regex text)))
                                        ens?     (when ens-regex
                                                   (boolean (re-matches ens-regex text)))]
                                    (set-value text)
                                    (if (> (count text) 0)
                                      (set-status :typing)
                                      (set-status :active))
                                    (when on-change-text
                                      (on-change-text text))
                                    (when (and on-detect-ens ens?)
                                      (set-status :loading)
                                      (on-detect-ens text #(set-status :typing)))
                                    (when (and address? on-detect-address)
                                      (set-status :loading)
                                      (on-detect-address text))
                                    (when (and (not address?)
                                               (not ens?)
                                               on-detect-unclassified)
                                      (on-detect-unclassified text)))))
        on-paste               (rn/use-callback #(if (fn? on-paste)
                                                   (on-paste on-change)
                                                   (clipboard/get-string
                                                    (fn [clipboard]
                                                      (when-not (empty? clipboard)
                                                        (on-change clipboard)))))
                                                [on-paste])
        on-clear               (rn/use-callback
                                (fn []
                                  (on-change "")
                                  (set-status (if focused? :active :default))
                                  (when on-change-text
                                    (on-change-text ""))
                                  (when on-clear
                                    (on-clear)))
                                [focused?])
        on-scan                (rn/use-callback #(when on-scan (on-scan on-change))
                                                [on-scan])
        on-focus               (rn/use-callback
                                (fn []
                                  (when (= (count value) 0)
                                    (set-status :active))
                                  (set-focused true)
                                  (when on-focus (on-focus))))
        on-blur                (rn/use-callback
                                (fn []
                                  (when (= status :active)
                                    (set-status :default))
                                  (set-focused false)
                                  (when on-blur (on-blur)))
                                [status])
        placeholder-text-color (rn/use-memo #(get-placeholder-text-color status theme blur?)
                                            [status theme blur?])]
    (rn/use-mount #(on-change (or default-value "")))
    [rn/view {:style (style/container container-style)}
     [rn/text-input
      {:accessibility-label    :address-text-input
       :style                  (style/input-text theme)
       :placeholder            (i18n/label :t/name-ens-or-address)
       :placeholder-text-color placeholder-text-color
       :value                  value
       :auto-complete          (when platform/ios? :off)
       :auto-capitalize        :none
       :auto-correct           false
       :spell-check            false
       :multiline              true
       :keyboard-appearance    theme
       :on-focus               on-focus
       :on-blur                on-blur
       :on-change-text         on-change}]
     (when (empty? value)
       [rn/view
        {:style               style/buttons-container
         :accessibility-label :paste-scan-buttons-container}
        [button/button
         {:accessibility-label :paste-button
          :type                :outline
          :size                24
          :container-style     {:margin-right 8}
          :inner-style         (style/accessory-button blur? theme)
          :on-press            on-paste}
         (i18n/label :t/paste)]
        (when on-scan
          [button/button
           {:accessibility-label :scan-button
            :icon-only?          true
            :type                :outline
            :size                24
            :inner-style         (style/accessory-button blur? theme)
            :on-press            on-scan}
           :main-icons/scan])])
     (when (or (= status :typing)
               (and (= status :active)
                    (not-empty value)))
       [rn/view
        {:style               style/buttons-container
         :accessibility-label :clear-button-container}
        [clear-button
         {:on-press on-clear
          :blur?    blur?
          :theme    theme}]])
     (when (and (= status :loading) (not valid-ens-or-address?))
       [rn/view
        {:style               style/buttons-container
         :accessibility-label :loading-button-container}
        [loading-icon blur? theme]])
     (when (and (or (= status :loading)
                    (= status :default))
                valid-ens-or-address?
                (not-empty value))
       [rn/view
        {:style               style/buttons-container
         :accessibility-label :positive-button-container}
        [positive-state-icon theme]])]))
