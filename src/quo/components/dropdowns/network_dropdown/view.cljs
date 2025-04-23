(ns quo.components.dropdowns.network-dropdown.view
  (:require
    [quo.components.common.new-feature-dot :as new-feature-dot]
    [quo.components.dropdowns.network-dropdown.style :as style]
    [quo.components.icon :as icon]
    [quo.components.list-items.preview-list.view :as preview-list]
    [quo.components.markdown.text :as text]
    [quo.context :as quo.context]
    [react-native.core :as rn]))

(defn dropdown-icon
  [theme]
  (let [{:keys [background foreground]} (style/dropdown-icon-colors theme)]
    [rn/view {:style style/dropdown-icon-container}
     [icon/icon
      :i/dropdown
      {:size                20
       :accessibility-label :dropdown-icon
       :color               background
       :color-2             foreground}]]))

(defn view
  [{:keys [on-press state show-new-chain-indicator? label dropdown-icon?] :as props} networks]
  (let [theme                  (quo.context/use-theme)
        [pressed? set-pressed] (rn/use-state false)
        on-press-in            (rn/use-callback #(set-pressed true))
        on-press-out           (rn/use-callback #(set-pressed false))]
    [rn/pressable
     {:style               (style/dropdown-container (merge props {:pressed? pressed? :theme theme}))
      :accessibility-label :network-dropdown
      :disabled            (= state :disabled)
      :on-press            on-press
      :on-press-in         on-press-in
      :on-press-out        on-press-out}
     (when show-new-chain-indicator?
       [new-feature-dot/view
        {:style               style/new-chain-indicator
         :accessibility-label :new-chain-indicator}])
     [rn/view {:flex-direction :row}
      [preview-list/view
       {:type      :network
        :list-size (count networks)
        :size      :size-20}
       networks]
      (when label
        [text/text
         {:size            :paragraph-1
          :weight          :medium
          :style           (style/dropdown-text theme)
          :number-of-lines 1}
         label])
      (when dropdown-icon?
        [dropdown-icon theme])]]))
