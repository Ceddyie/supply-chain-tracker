import type { LatLngExpression } from "leaflet";
import {MapContainer, Marker, Popup, TileLayer} from "react-leaflet";

type Props = {
    lat: number,
    lng: number,
    label?: string,
};

export function CurrentLocationMap({ lat, lng, label }: Props) {
    const center: LatLngExpression = [lat, lng];

    return (
        <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
            <div className="flex items-center justify-between">
                <h3 className="text-sm font-medium text-white">Current Location</h3>
                <span className="text-xs text-white/60">
                    {lat.toFixed(5)}, {lng.toFixed(5)}
                </span>
            </div>

            <div className="mt-3 overflow-hidden rounded-xl border border-white/10">
                <MapContainer center={center} zoom={13} scrollWheelZoom={false} className="h-64 w-full">
                    <TileLayer
                        attribution="&copy; OpenStreetMap contributors"
                        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                    />
                    <Marker position={center}>
                        <Popup>{label ?? "Current checkpoint"}</Popup>
                    </Marker>
                </MapContainer>
            </div>
        </div>
    );
}