(ns quo.components.utilities.token.view
  (:require [quo.components.utilities.token.loader :as token-loader]
            [react-native.core :as rn]
            [utils.number]))

(defn- size->number
  "Remove `size-` prefix in size keywords and returns a number useful for styling."
  [size]
  (-> size name (subs 5) utils.number/parse-int))

(defn- token-style
  [style size]
  (let [size-number (if (keyword? size)
                      (size->number size)
                      size)]
    (assoc style
           :width  size-number
           :height size-number)))

(def ^:private b64-png-image-prefix "data:image/png;base64,")

(defn view
  "Render a token image.
   Props:
   - style:           extra styles to apply to the `rn/image` component.
   - size:            `:size-nn` or just `nn`, being `nn` any number. Defaults to 32.
   - token:           string or keyword, it can contain upper case letters or not.
                      E.g. all of these are valid and resolve to the same:
                      :token/snt | :snt | :SNT | \"SNT\" | \"snt\".
   - image-source:    Ignores `token` and uses this as parameter to `rn/image`'s source.
   - add-b64-prefix?: If true, adds `data:image/png;base64,` as prefix to the string
                      passed as `image-source`.
  "
  [{:keys [token size style image-source add-b64-prefix?]
    :or   {size  32
           token :snt}}]
  (let [b64-string (if (and image-source add-b64-prefix?)
                     (str b64-png-image-prefix image-source)
                     image-source)
        source     (or b64-string (token-loader/get-token-image token))]
    [rn/image
     {:style  (token-style style size)
      :source source}]))
