use strict;
use ElectricCommander ();
$| = 1;
my $ec = new ElectricCommander->new();

my @versions = split(",","$[_versions]");
my $artifactGroup = "$[_artifactGroup]";
my @artifactKeys = split(",","$[_artifactKeys]");

for (my $i=0 ; $i< scalar @artifactKeys ; $i++) {
	print $ec->createJobStep({
		jobStepName => "${artifactGroup}:@artifactKeys[$i]:@versions[$i]",
		parallel => 0,
		subproject => '/plugins/EC-Artifact/project',
		subprocedure => 'Publish',
		actualParameter => [
			{ actualParameterName => 'artifactName', 
				value => "${artifactGroup}:@artifactKeys[$i]" },
			{ actualParameterName => 'artifactVersionVersion',
				value => @versions[$i]},
			{ actualParameterName => 'fromLocation',
				value => '/tmp'},
			{ actualParameterName => 'includePatterns',
				value => 'empty'},
			{ actualParameterName => 'repositoryName',
				value => 'default'}
		]
	})->findnodes_as_string("/");
}