function [tonalList, headerInfo] = dtTonalsLoad(Filename, gui)
% tonalList = dtTonalsLoad(Filename, gui)
% Load a set of tonals from Filename.  The user is prompted for
% the file to load if one of the following is true:
%   No arguments are provided
%   Filename is [] 
%   or gui is true (uses Filename as default)
%
% Examples:
%  [tonals, info] = dtTonalsLoad('palmyra092007FS192-071011-230000.bin');
%  [tonals, info] = dtTonalsLoad();
%  [tonals, info] = dtTonalsLoad('detections.det', true);
%
% Returns a Java collection of tonals.

import tonals.*

error(nargchk(1,2,nargin));
if nargin < 2
    if isempty(Filename)
        gui = true;
    else
        gui = false;
    end
end

if gui
    [LoadFile, LoadDir] = uigetfile({'*.bin'; '*.det'; '*.ton'; '*.*'},...
        'Load Tonals', Filename);
    
    % check for cancel
    if isnumeric(LoadFile)
        tonalList = [];
        return
    else
        Filename = fullfile(LoadDir, LoadFile);
    end
end

[path name ext] = fileparts(Filename);
if strcmp(ext, '.ton')
    % loads objects
    tonalList = tonals.tonal.tonalsLoad(Filename);
    headerInfo = [];
else
    % loads binary file
    instream = TonalBinaryInputStream;
    
    instream.tonalBinaryInputStream(Filename);    % retrieve linked list
    tonalList = instream.getTonals;
    headerInfo = instream.getHeader;
end
