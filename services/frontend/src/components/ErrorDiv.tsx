export function ErrorDiv({error} : { error: string }) {
    return (
        <div role="alert"
             className="mb-4 rounded-xl border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-100">
            <div className="font-medium text-red-100">Shipment creation failed</div>
            <div className="text-red-100/80">{error}</div>
        </div>
    )
}