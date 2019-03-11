(ns status-im.ui.screens.add-new.models
  (:require [cljs.spec.alpha :as spec]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.universal-links.core :as universal-links]
            [status-im.utils.fx :as fx]))

(fx/defn handle-qr-code
  [cofx data]
  (if (spec/valid? :global/public-key data)
    (universal-links/handle-view-profile cofx data)
    (or (universal-links/handle-url cofx data)
        {:utils/show-popup {:title   (i18n/label :t/unable-to-read-this-code)
                            :content (i18n/label :t/use-valid-qr-code {:data data})
                            :on-dismiss #(re-frame/dispatch [:qr-scanner.ui/qr-code-error-dismissed])}})))
