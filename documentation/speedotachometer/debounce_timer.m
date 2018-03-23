% Lm = length of the magnet to be mounted on the axle
Lm = 3E-2; % meters

% Dt = diamter of car tire
Dt = 21.5; % inches
Dt = Dt / 39.370; % convert inches to meters
% Da = diamter of axle where the magnet is mounted
Da = 70E-3; % 71 mm

% fs = sampling frequency
fs = 1E3; % Hz
% Ts = sampling period
Ts = 1/fs; % seconds
% Td = debounce time
% set to a range for visual analysis
Td = 2*Ts:1E-3:100E-3; % seconds

% Vv is the speed of the vehicle at the tires.
% These two design parameters may not be possible to reach at the same time
%
% Vv_maxTarget is the max speed at which the system is being designed for
Vv_maxTarget = 40; % mph (miles per hour)
Vv_maxTarget = Vv_maxTarget * 0.44704; % convert mph to meters / second
% Vv_minTarget is the min speed at which the system is being designed for
Vv_minTarget = 1; % mph (miles per hour)
Vv_minTarget = Vv_minTarget * 0.44704; % convert mph to meters / second

% ========================================================
% MINIMUM MAGNET LENGTH
% ========================================================
% Limiting factor: sampling frequency
% The smaller magnet the better
Lm_min = 2 * Ts * Da * Vv_maxTarget / Dt; % meters
% Mar 8, 2018: 5mm.
% Going to shoot for 1 cm.
% Note that if this is actually set for 5mm then the Vv targets WILL
% actually be met, theoretically. However, I'd prefer this more conservative
% magnet length.
Lm = 1E-2; % meters

% ========================================================
% MAX DEBOUNCE TIME
% ========================================================
% The maximum and minimum measurable speeds are limited by the debounce time
carVelocityMin = Lm * Dt ./ (Td * Da); % meters / second
carVelocityMax = (Da * pi - Lm) * Dt ./ (Td * Da); % meters / second

minMph = carVelocityMin * 2.23694;
maxMph = carVelocityMax * 2.23694;

% plotting the relationships between Td and the min/max mph speeds
figure(1);
clf;
linewidth = 3;
plot(Td * 1000, minMph, 'displayname', 'Min MPH', 'linewidth', linewidth);
hold on;
plot(Td * 1000, maxMph, 'displayname', 'Max MPH', 'linewidth', linewidth);
legend('show', 'location', 'best');
title('Debounce Time and Speed Limitations');
xlabel('Td: Debounce Time (ms)');
ylabel('MPH');
grid on;
grid minor;
% Graph reduced to interesting sections
ylim([0, 50]);
xlim([50, 100]);
set(gca, 'fontsize', 15);
% Mar 8, 2018: choosing Td = 90 ms

% Td chosen
Td = 90E-3; % seconds

carVelocityMin = Lm * Dt / (Td * Da); % meters / second
carVelocityMax = (Da * pi - Lm) * Dt / (Td * Da); % meters / second

maxRevolutionTime = Dt * pi / carVelocityMin % seconds

minMph = carVelocityMin * 2.23694;
maxMph = carVelocityMax * 2.23694;


% RPM needed for test motor
Vmax = 17.88; % m/s
rt = Dt / 2; % m
w = Vmax / rt; % ?*r = V. ? in rad/s
rps = w / (2 * pi); % rad/s * rev/(2? rad) = rev/s
rpm = rps * 60 % rev/s * 60 s/min = rev/min
% need rpm of 650



